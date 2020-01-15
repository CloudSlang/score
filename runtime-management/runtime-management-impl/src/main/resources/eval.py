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

        sys_prop = payload["context"]["sys_prop"]
        env_setup = payload["envSetup"]
        get_sp = None
        get = None
        exec (env_setup, globals())

    def __wrapValues(self, value):
        return AccessWrapper(value)

    def main(self):
        try:
            raw_inputs = input().encode(sys.stdin.encoding).decode()
            payload = json.loads(raw_inputs)

            expression = payload["expression"]
            context = payload["context"]
            print(context)
            self.__init_context(payload)

            smallerContext = {"get_sp": get_sp, "get": get}

            old_io = self.__disable_standard_io()
            try:
                final_result = {"returnResult": eval(expression, smallerContext)}
            finally:
                self.__enable_standard_io(old_io)
        except Exception as e:
            final_result = {"exception": str(e)}

        print(json.dumps(final_result))



class AccessWrapper(str):
    def __init__(self, value):
        self.__value = value
        print('init with value: ', value)

    def __enter__(self):
        print('enter with  value', self.__value)
        return self

    def __str__(self):
        print('str with  value', self.__value)
        return self.__value

    def __repr__(self):
        print('repr with  value', self.__value)
        return repr(self.__value)


if __name__ == '__main__':
    PythonAgentExecutor().main()
