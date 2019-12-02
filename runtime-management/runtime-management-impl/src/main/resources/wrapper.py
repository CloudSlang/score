import importlib
import inspect

ENCRYPTED_PREFIX = "{ENCRYPTED}"
OBFUSCATED_PREFIX = "{OBFUSCATED}"


def process_input(key, value):
    return key, value


def __decrypt_inputs__(encrypted_inputs: dict):
    return dict(map(lambda user_input: process_input(*user_input), encrypted_inputs.items()))


def __encrypt__(value):
    return OBFUSCATED_PREFIX + str(value)


def __encrypt_outputs__(result, outputs: dict):
    return dict(map(lambda output: process_output(*output, result), outputs.items()))


def process_output(key, is_sensitive, result: dict):
    value = str(result[key] if key in result else "")
    return key, __encrypt__(value) if is_sensitive else value


def __validate_arguments__(actual_input_list, script):
    if sorted(inspect.getfullargspec(getattr(script, "execute"))[0]) != sorted(actual_input_list):
        raise Exception("Missing required input!")


def execute_action(script_name, enc_key, encrypted_inputs, outputs):
    script = importlib.import_module(script_name)
    __validate_arguments__(encrypted_inputs.keys(), script)
    decrypted_inputs = __decrypt_inputs__(encrypted_inputs)
    result = getattr(script, "execute")(**decrypted_inputs)
    if isinstance(result, dict):
        final_result = __encrypt_outputs__(result, outputs)
    else:
        final_result = {"returnResult": str(result)}
    return final_result
