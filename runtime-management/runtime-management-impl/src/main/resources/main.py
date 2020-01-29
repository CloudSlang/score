import importlib
import inspect
import json
import os
import sys
import traceback
from traceback import StackSummary

EXECUTE_METHOD = "execute"


# noinspection PyMethodMayBeStatic


class PythonAgentExecutor(object):
    # error formatting, returns a list of formatted traceback
    def __format(self, list):
        # Similar with StackSummary format but without filename
        _RECURSIVE_CUTOFF = 3
        result = []
        last_file = None
        last_line = None
        last_name = None
        count = 0
        for frame in list:
            if (last_file is None or last_file != frame.filename or
                last_line is None or last_line != frame.lineno or
                last_name is None or last_name != frame.name):
                if count > _RECURSIVE_CUTOFF:
                    count -= _RECURSIVE_CUTOFF
                    # Enable when we show full traceback
                    #result.append(
                    #    f'  [Previous line repeated {count} more '
                    #    f'time{"s" if count > 1 else ""}]\n'
                    #)
                last_file = frame.filename
                last_line = frame.lineno
                last_name = frame.name
                count = 0
            count += 1
            if count > _RECURSIVE_CUTOFF:
                continue
            row = []
            row.append('  line {}, in {}\n'.format(
                frame.lineno, frame.name))
            if frame.line:
                row.append('    {}\n'.format(frame.line.strip()))
            if frame.locals:
                for name, value in sorted(frame.locals.items()):
                    row.append('    {name} = {value}\n'.format(name=name, value=value))
            result.append(''.join(row))
        if count > _RECURSIVE_CUTOFF:
            count -= _RECURSIVE_CUTOFF
            # Enable when we show full traceback
            #result.append(
            #    f'  [Previous line repeated {count} more '
            #    f'time{"s" if count > 1 else ""}]\n'
            #)
        return result

    def __from_list(self, extracted_list):
        return self.__format(StackSummary.from_list(extracted_list))
    # end of error formatting

    def __validate_arguments(self, actual_input_list, script):
        expected_inputs = sorted(inspect.getfullargspec(getattr(script, EXECUTE_METHOD))[0])
        actual_inputs = sorted(actual_input_list)
        if expected_inputs != actual_inputs:
            raise Exception("Expected inputs " + str(expected_inputs) +
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

    def __check_output_type(self, result):
        for output in result.items():
            if type(output[1]) != str:
                raise Exception("Error binding output: '" + str(output[0]) +
                                "' should be of type str, but got value '" + str(output[1]) +
                                "' of type " + type(output[1]).__name__)

    def __process_result(self, result):
        if result is None:
            return {"returnResult": {}}
        if isinstance(result, dict):
            self.__check_output_type(result)
            final_result = {"returnResult": dict(map(lambda output: (str(output[0]), str(output[1])), result.items()))}
        else:
            final_result = {"returnResult": {"returnResult": str(result)}}
        return final_result

    def main(self):
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
        except Exception as e:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            final_result = {
                "exception": str(e),
                "traceback": self.__from_list(traceback.extract_tb(exc_tb))
            }

        print(json.dumps(final_result))


if __name__ == '__main__':
    PythonAgentExecutor().main()
