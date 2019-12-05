import json
import sys

from wrapper import PythonExecutionWrapper


class PythonAgentExecutor(object):
    # noinspection PyMethodMayBeStatic
    def __disable_standard_io(self):
        old_io = (sys.stdin, sys.stdout, sys.stderr, sys.exit)
        sys.stdin, sys.stdout, sys.stderr, sys.exit = (None, None, None, lambda *x, **y: None)
        return old_io

    # noinspection PyMethodMayBeStatic
    def __enable_standard_io(self, old_io):
        (sys.stdin, sys.stdout, sys.stderr, sys.exit) = old_io

    # noinspection PyMethodMayBeStatic
    def main(self):
        try:
            # todo remove dummy
            payload = json.loads(input())

            script_name = payload["script_name"]
            inputs = payload["inputs"]

            old_io = self.__disable_standard_io()
            try:
                result = PythonExecutionWrapper().execute_action(script_name, inputs)
            finally:
                self.__enable_standard_io(old_io)
        except Exception as e:
            result = {"exception": str(e)}

        print(json.dumps(result))


if __name__ == '__main__':
    PythonAgentExecutor().main()