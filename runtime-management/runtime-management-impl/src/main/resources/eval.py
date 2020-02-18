import json
import sys


# noinspection PyMethodMayBeStatic
class PythonAgentExecutor(object):

    def __get_accessed_method(self, key):
        accessed_resources_set.add(key)

    def __disable_standard_io(self):
        old_io = (sys.stdin, sys.stdout, sys.stderr, sys.exit)
        sys.stdin, sys.stdout, sys.stderr, sys.exit = (None, None, None, lambda *x, **y: None)
        return old_io

    def __enable_standard_io(self, old_io):
        (sys.stdin, sys.stdout, sys.stderr, sys.exit) = old_io

    def __init_context(self, payload):
        global sys_prop
        global get_sp
        global get
        global accessed
        global accessed_resources_set
        accessed_resources_set = set()
        context = payload["context"]

        if "sys_prop" in context:
            sys_prop = context["sys_prop"]
            accessed = self.__get_accessed_method

        env_setup = payload["envSetup"]
        get_sp = None
        get = None
        exec (env_setup, globals())

    def main(self):
        try:
            raw_inputs = input().encode(sys.stdin.encoding).decode()
            payload = json.loads(raw_inputs)
            expression = payload["expression"]
            context = payload["context"]
            self.__init_context(payload)

            smaller_context = AccessAwareDict({"get_sp": get_sp, "get": get})

            for x in dir(__builtins__):
                smaller_context[x] = eval(x)

            for key, var in context.items():
                if key in smaller_context:
                    raise Exception(f"Conflicting variable names: {key}")
                smaller_context[key] = var

            old_io = self.__disable_standard_io()

            try:
                expr_result = eval(expression, smaller_context)
                return_type = type(expr_result).__name__
                if return_type not in ['str', 'int', 'bool']:
                    return_type = 'str'
                final_result = {"returnResult": expr_result,
                                "accessedResources": list(accessed_resources_set),
                                "returnType": return_type}
            finally:
                self.__enable_standard_io(old_io)
        except Exception as e:
            final_result = {"exception": str(e)}

        print(json.dumps(final_result))


class AccessAwareDict(dict):
    def __getitem__(self, name):
        accessed_resources_set.add(name)
        if not self.__contains__(name):
            raise NameError(f"name '{name}' is not defined")
        return self.get(name)

if __name__ == '__main__':
    PythonAgentExecutor().main()
