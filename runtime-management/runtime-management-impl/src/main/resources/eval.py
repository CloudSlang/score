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

    def __wrapValue(self, value):
        if isinstance(value, str):
            return AccessWrapper(value)
        return value

    def main(self):
        #try:
        # raw_inputs = input().encode(sys.stdin.encoding).decode()
        # payload = json.loads(raw_inputs)
        #
        # expression = payload["expression"]
        # context = payload["context"]
        # print(context)
        # self.__init_context(payload)
        #
        # smallerContext = {"get_sp": get_sp, "get": get}
        #
        # old_io = self.__disable_standard_io()
        # try:
        #     final_result = {"returnResult": eval(expression, smallerContext)}
        # finally:
        #     self.__enable_standard_io(old_io)

        val = self.__wrapValue('string_value')
        print(val)
        print(val + ' additional')
        #print(val.get_parent())
        #print(int(val))
        #print(len(val))
        #print(val.capitalize)
        #except Exception as e:
        #final_result = {"exception": str(e)}

        #print(json.dumps(final_result))


def delegate(method, prop):
    def decorate(cls):
        setattr(cls, method,
                lambda self, *args, **kwargs:
                do_action(self, cls, method, prop, *args, **kwargs))
        return cls
    return decorate

def do_action(context, cls, method, prop, *args, **kwargs):
    print('acessed')
    setattr(cls, '__accessed', True)
    return getattr(getattr(context, prop), method)(*args, **kwargs)

@delegate('__str__', '_string_value')
@delegate('__add__', '_string_value')
@delegate('repr', '_string_value')
@delegate('print', '_string_value')
class AccessWrapper(str):
    def __init__(self, value):
        self._string_value = value
        self.__accessed = False


if __name__ == '__main__':
    PythonAgentExecutor().main()
