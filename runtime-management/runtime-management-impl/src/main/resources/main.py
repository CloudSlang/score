import json
import sys

from wrapper import PythonExecutionWrapper


class PythonAgentExecutor(object):
    # noinspection PyMethodMayBeStatic
    def __create_dummy_payload(self):
        return json.dumps({
            "script_name": "py_script.script",
            "var": [29, 123, 161, 193, 70, 162, 1, 212, 253, 215, 51, 111, 250, 45, 62, 92],
            "inputs": {
                "input1": "{ENCRYPTED}QEuOifO9sUg3fQgwJXuKbA==",
                "input2": "55"
            },
            "outputs": {
                "output1": False,
                "output2": False,
            }
        })

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
            payload = json.loads(input() or self.__create_dummy_payload())

            script_name = payload["script_name"]
            enc_key = bytearray(payload["var"])
            inputs = payload["inputs"]
            outputs = payload["outputs"]

            old_io = self.__disable_standard_io()
            try:
                result = PythonExecutionWrapper(enc_key).execute_action(script_name, inputs, outputs)
            finally:
                self.__enable_standard_io(old_io)
        except Exception as e:
            result = {"exception": str(e)}

        print(json.dumps(result))


if __name__ == '__main__':
    PythonAgentExecutor().main()
