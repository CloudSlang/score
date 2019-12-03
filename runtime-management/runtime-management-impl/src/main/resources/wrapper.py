import importlib
import inspect

from utils import SymmetricEncryptor

ENCRYPTED_PREFIX = "{ENCRYPTED}"
UTF_8 = "utf-8"
EXECUTE_METHOD = "execute"


class PythonExecutionWrapper(object):
    def __init__(self, key):
        self.encryptor = SymmetricEncryptor(key)

    def __decrypt_input(self, key, value):
        if value.startswith(ENCRYPTED_PREFIX):
            return key, self.encryptor.decrypt(value[len(ENCRYPTED_PREFIX):]).decode(UTF_8)
        return key, value

    def __decrypt_inputs(self, encrypted_inputs: dict):
        return dict(map(lambda user_input: self.__decrypt_input(*user_input), encrypted_inputs.items()))

    def __encrypt_outputs(self, result, outputs: dict):
        return dict(map(lambda output: self.__process_output(*output, result), outputs.items()))

    def __process_output(self, key, is_sensitive, result: dict):
        value = str(result[key] if key in result else "")
        return key, ENCRYPTED_PREFIX + self.encryptor.encrypt(value).decode(UTF_8) if is_sensitive else value

    # noinspection PyMethodMayBeStatic
    def __validate_arguments(self, actual_input_list, script):
        expected_inputs = sorted(inspect.getfullargspec(getattr(script, EXECUTE_METHOD))[0])
        actual_inputs = sorted(actual_input_list)
        if expected_inputs != actual_inputs:
            raise Exception("Expected inputs " + str(expected_inputs) +
                            " are not the same with the actual inputs " + str(actual_inputs))

    def execute_action(self, script_name, inputs, outputs):
        script = importlib.import_module(script_name)
        self.__validate_arguments(inputs.keys(), script)

        decrypted_inputs = self.__decrypt_inputs(inputs)
        result = getattr(script, EXECUTE_METHOD)(**decrypted_inputs)
        if isinstance(result, dict):
            final_result = self.__encrypt_outputs(result, outputs)
        else:
            final_result = {"returnResult": str(result)}
        return final_result
