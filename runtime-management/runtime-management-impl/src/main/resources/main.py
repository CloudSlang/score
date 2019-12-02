import json
import sys

import wrapper


def __create_dummy_payload__():
    return json.dumps({
        "script_name": "py_script.script",
        "enc_key": "Am@yzI|\\|g\n",
        "inputs": {
            "input1": "56",
            "input2": "67"
        },
        "outputs": {
            "output1": True,
            "output2": False
        }
    })


def __disable_standard_io__():
    old_io = (sys.stdin, sys.stdout, sys.stderr, sys.exit)
    sys.stdin, sys.stdout, sys.stderr, sys.exit = (None, None, None, lambda *x, **y: None)
    return old_io


def __enable_standard_io__(old_io):
    (sys.stdin, sys.stdout, sys.stderr, sys.exit) = old_io


def main():
    try:
        # todo remove dummy
        payload = json.loads(input("Payload: ") or __create_dummy_payload__())

        script_name = payload["script_name"]
        enc_key = payload["enc_key"]
        inputs = payload["inputs"]
        outputs = payload["outputs"]

        old_io = __disable_standard_io__()
        try:
            result = wrapper.execute_action(script_name, enc_key, inputs, outputs)
        finally:
            __enable_standard_io__(old_io)
    except Exception as e:
        result = {"exception": str(e)}

    print(json.dumps(result))


if __name__ == '__main__':
    main()
