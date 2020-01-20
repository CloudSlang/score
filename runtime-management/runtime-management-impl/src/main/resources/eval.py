import json
import sys

EXECUTE_METHOD = "execute"


# noinspection PyMethodMayBeStatic


class PythonAgentExecutor(object):

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

        context = payload["context"]
        if("sys_prop" in context):
            sys_prop = context["sys_prop"]
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
            #print(context)
            self.__init_context(payload)

            smaller_context = {"get_sp": get_sp, "get": get}


            #print(__builtins__)
            for x in dir(__builtins__):
                #print(f"func {x}")
                smaller_context[x] = eval(x)

            for key, var in context.items():
                #if(isinstance(var, str)):
                #todo: make sure not to overwrite names
                #print(f"key: {key}, value: {var}")
                smaller_context[key] = var
            #print('before')
            #print("\n\n", smaller_context)

            old_io = self.__disable_standard_io()
            try:
                final_result = {"returnResult": eval(expression, smaller_context)}
                print('final')
            finally:
                self.__enable_standard_io(old_io)
        except Exception as e:
            final_result = {"exception": str(e)}

        print(json.dumps(final_result))


if __name__ == '__main__':
    PythonAgentExecutor().main()
