import importlib
import inspect
import json
import sys

EXECUTE_METHOD = "execute"
# noinspection PyMethodMayBeStatic


class PythonAgentExecutor(object):

    def __validate_arguments(self, actual_input_list, script):
        expected_inputs = sorted(inspect.getfullargspec(getattr(script, EXECUTE_METHOD))[0])
        actual_inputs = sorted(actual_input_list)
        if expected_inputs != actual_inputs:
            raise Exception("Expected inputs " + str(expected_inputs) +
                            " are not the same with the actual inputs " + str(actual_inputs))

    def __execute_action(self, script_name, inputs):
        script = importlib.import_module(script_name)
        self.__validate_arguments(inputs.keys(), script)

        return getattr(script, EXECUTE_METHOD)(**inputs)

    def __disable_standard_io(self):
        old_io = (sys.stdin, sys.stdout, sys.stderr, sys.exit)
        sys.stdin, sys.stdout, sys.stderr, sys.exit = (None, None, None, lambda *x, **y: None)
        return old_io

    def __enable_standard_io(self, old_io):
        (sys.stdin, sys.stdout, sys.stderr, sys.exit) = old_io

    def __process_result(self, result):
        if isinstance(result, dict):
            final_result = {"returnResult": dict(map(lambda output: (str(output[0]), str(output[1])), result.items()))}
        else:
            final_result = {"returnResult": {"returnResult": str(result)}}
        return final_result

    def main(self):
        try:
            payload = json.loads(input())

            script_name = payload["script_name"]
            inputs = payload["inputs"]

            old_io = self.__disable_standard_io()
            try:
                result = self.__execute_action(script_name, inputs)
                final_result = self.__process_result(result)
            finally:
                self.__enable_standard_io(old_io)
        except Exception as e:
            final_result = {"exception": str(e)}

        print(json.dumps(final_result))


if __name__ == '__main__':
    PythonAgentExecutor().main()
