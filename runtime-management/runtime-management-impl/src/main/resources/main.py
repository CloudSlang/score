import importlib
import inspect
import json
import os
import sys
import traceback

EXECUTE_METHOD = "execute"


# noinspection PyMethodMayBeStatic
class InvalidExecutionException(Exception):
    pass

class PythonAgentExecutor(object):

    def __validate_arguments(self, actual_input_list, script):
        expected_inputs = sorted(inspect.getfullargspec(getattr(script, EXECUTE_METHOD))[0])
        actual_inputs = sorted(actual_input_list)
        if expected_inputs != actual_inputs:
            raise InvalidExecutionException("Expected inputs " + str(expected_inputs) +
                                     " are not the same with the actual inputs " + str(actual_inputs))

    def __execute_action(self, script_name, inputs):
        sys.path.append(os.getcwd())
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
        if result is None:
            return {"returnResult": {}}
        if isinstance(result, dict):
            final_result = {"returnResult": dict(map(lambda output: (str(output[0]), str(output[1])), result.items()))}
        else:
            final_result = {"returnResult": {"returnResult": str(result)}}
        return final_result

    def print_event(self, event):
        print(event)

    def main(self):
        self.print_event("<data>")
        self.print_event("<execution>")
        try:
            raw_inputs = input().encode(sys.stdin.encoding).decode()
            payload = json.loads(raw_inputs)

            script_name = payload["script_name"]
            inputs = payload["inputs"]

            old_io = self.__disable_standard_io()
            try:
                result = self.__execute_action(script_name, inputs)
                final_result = self.__process_result(result)
            finally:
                self.__enable_standard_io(old_io)
                self.print_event("</execution>")
        except InvalidExecutionException as e:
            final_result = {
                "exception": str(e)
            }
        except Exception as e:
            exc_tb = sys.exc_info()[2]
            final_result = {
                "exception": str(e),
                "traceback": traceback.format_list(traceback.extract_tb(exc_tb))
            }
        self.print_event("<result>")
        print(json.dumps(final_result))
        self.print_event("</result>")
        self.print_event("</data>")


if __name__ == '__main__':
    PythonAgentExecutor().main()
