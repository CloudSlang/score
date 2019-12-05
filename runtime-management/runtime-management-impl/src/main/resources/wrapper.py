import importlib
import inspect


ENCRYPTED_PREFIX = "{ENCRYPTED}"
UTF_8 = "utf-8"
EXECUTE_METHOD = "execute"


class PythonExecutionWrapper(object):
    # noinspection PyMethodMayBeStatic
    def __validate_arguments(self, actual_input_list, script):
        expected_inputs = sorted(inspect.getfullargspec(getattr(script, EXECUTE_METHOD))[0])
        actual_inputs = sorted(actual_input_list)
        if expected_inputs != actual_inputs:
            raise Exception("Expected inputs " + str(expected_inputs) +
                            " are not the same with the actual inputs " + str(actual_inputs))

    def execute_action(self, script_name, inputs):
        script = importlib.import_module(script_name)
        self.__validate_arguments(inputs.keys(), script)

        result = getattr(script, EXECUTE_METHOD)(**inputs)
        if isinstance(result, dict):
            final_result = dict(map(lambda output: (output[0], str(output[1])), result.items()))
        else:
            final_result = {"returnResult": str(result)}
        return final_result