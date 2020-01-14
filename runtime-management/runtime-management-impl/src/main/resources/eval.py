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

    def main(self):
        try:
            raw_inputs = input().encode(sys.stdin.encoding).decode()
            payload = json.loads(raw_inputs)

            expression = payload["expression"]
            #inuts = payload["inputs"]

            old_io = self.__disable_standard_io()
            try:
                #sys.path.append(os.getcwd())
                #script = importlib.import_module(script_name)
                final_result= {"returnResult": eval(expression)}
                # result = self.__execute_action(script_name, inputs)
                # final_result = self.__process_result(result)
            finally:
                self.__enable_standard_io(old_io)
        except Exception as e:
            final_result = {"exception": str(e)}

        print(json.dumps(final_result))

if __name__ == '__main__':
    PythonAgentExecutor().main()
