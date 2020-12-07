import json
import sys
from jsonpath_ng.ext import parse
import re
from io import StringIO
from lxml import etree

class PythonAgentExecutor(object):

    def __get_accessed_method(self, key):
        accessed_resources_set.add(key)

    def __disable_standard_io(self):
        old_io = (sys.stdin, sys.stdout, sys.stderr, sys.exit)
        sys.stdin, sys.stdout, sys.stderr, sys.exit = (None, None, None, lambda *x, **y: None)
        return old_io

    def __enable_standard_io(self, old_io):
        (sys.stdin, sys.stdout, sys.stderr, sys.exit) = old_io

    def cs_regex(self, str, regex, split_lines = False):
        lines = str.splitlines() if split_lines else [ str ]

        result = []
        for line in lines:
            x = re.findall(regex, line)
            result.extend(x)

        if len(result) == 0:
            return None

        if len(result) == 1:
            return result[0]

        return json.dumps(result)

    def cs_xpath_query(self, str, xpath):
        f = StringIO(str)
        tree = etree.parse(f)
        r = tree.xpath(xpath)
        return json.dumps(list(map(lambda val: etree.tostring(val, encoding="UTF-8").decode("UTF-8"), r))) if r is not None and len(r) > 0 else None

    def cs_json_query(self, str, json_path):
        json_data = json.loads(str)
        jsonpath_expr = parse(json_path)
        x = jsonpath_expr.find(json_data)
        return json.dumps(list(map(lambda val: val.value, x))) if x is not None and len(x) > 0 else None

    def get_from_smaller_context(self, key):
        return smaller_context[key]

    def __init_context(self, payload):
        global sys_prop
        global get_sp
        global get
        global cs_append
        global cs_prepend
        global cs_replace
        global cs_round
        global cs_extract_number
        global cs_substring
        global cs_to_upper
        global cs_to_lower
        global accessed
        global accessed_resources_set
        global get_from_smaller_context

        get_from_smaller_context = self.get_from_smaller_context
        accessed_resources_set = set()
        context = payload['context']

        if 'sys_prop' in context:
            sys_prop = context['sys_prop']
            accessed = self.__get_accessed_method

        env_setup = payload['envSetup']
        get_sp = None
        get = None
        cs_append = None
        cs_prepend = None
        cs_replace = None
        cs_round = None
        cs_extract_number = None
        cs_substring = None
        cs_to_upper = None
        cs_to_lower = None
        exec (env_setup, globals())

    def main(self):
        global smaller_context
        try:
            raw_inputs = input().encode(sys.stdin.encoding).decode()
            payload = json.loads(raw_inputs)
            expression = payload['expression']
            context = payload['context']
            self.__init_context(payload)

            smaller_context = AccessAwareDict({'get_sp': get_sp,
                                               'get': get,
                                               'cs_append': cs_append,
                                               'cs_prepend': cs_prepend,
                                               'cs_replace': cs_replace,
                                               'cs_round': cs_round,
                                               'cs_extract_number': cs_extract_number,
                                               'cs_substring': cs_substring,
                                               'cs_to_upper': cs_to_upper,
                                               'cs_to_lower': cs_to_lower,
                                               'cs_regex': self.cs_regex,
                                               'cs_xpath_query': self.cs_xpath_query,
                                               'cs_json_query': self.cs_json_query,
                                               })

            for x in dir(__builtins__):
                smaller_context[x] = eval(x)

            for key, var in context.items():
                smaller_context[key] = var

            old_io = self.__disable_standard_io()

            try:
                expr_result = eval(expression, smaller_context)
                return_type = type(expr_result).__name__

                if return_type == 'range':
                    expr_result = str(list(map(str, expr_result)))
                    return_type = 'list'

                elif return_type == 'list':
                    expr_result = str(expr_result)

                elif return_type in ['map',  'tuple',  'set']:
                    expr_result = str(list(expr_result))
                    return_type = 'list'

                elif return_type == 'dict':
                    expr_result = str(list(expr_result.keys()))
                    return_type = 'list'

                elif return_type == '_Element':
                    expr_result = etree.tostring(expr_result, encoding='UTF-8').decode('UTF-8')
                    return_type = 'str'

                if return_type not in ['str', 'int', 'bool', 'list']:
                    return_type = 'str'

                final_result = {'returnResult': expr_result,
                                'accessedResources': list(accessed_resources_set),
                                'returnType': return_type}
            finally:
                self.__enable_standard_io(old_io)
        except Exception as e:
            final_result = {'exception': str(e)}

        print(json.dumps(final_result))


class AccessAwareDict(dict):
    def __getitem__(self, name):
        accessed_resources_set.add(name)
        if not self.__contains__(name):
            raise NameError('name ' + name + ' is not defined')
        return self.get(name)

if __name__ == '__main__':
    PythonAgentExecutor().main()
