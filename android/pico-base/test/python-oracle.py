#!/usr/bin/env python3
"""Execute the existing Python calculations via AST, without network/MQTT setup."""
import ast
import copy
import json
import sys
from datetime import datetime

source = ast.parse(open(sys.argv[1], encoding='utf-8').read())
names = {'createSensorList', 'toTemperature', 'readBaro', 'readTemp', 'readTank',
         'readBatt', 'readBattNameVoltage', 'readVolt', 'readOhm', 'readCurrent', 'readIncline'}
functions = [n for n in source.body if isinstance(n, ast.FunctionDef) and n.name in names]
assert {n.name for n in functions} == names, 'Python baseline changed: review the oracle'
output_assignment = next(n for n in ast.walk(source) if isinstance(n, ast.Assign)
                         and any(isinstance(t, ast.Name) and t.id == 'output' for t in n.targets))
output_loop = next(n for n in ast.walk(source) if isinstance(n, ast.For)
                   and isinstance(n.target, ast.Tuple)
                   and [getattr(t, 'id', '') for t in n.target.elts] == ['sensorId', 'sensorData'])
namespace = {}
exec(compile(ast.Module(body=functions, type_ignores=[]), '<original-functions>', 'exec'), namespace)

class FixedDatetime:
    @staticmethod
    def now():
        if data.get('time'):
            return datetime(**data['time'])
        return datetime(2026, 10, 7, 12, 34, 56)

data = json.load(sys.stdin)
config = {int(k): {int(f): v for f, v in entry.items()} for k, entry in data['config'].items()}
element = {int(k): v for k, v in data['element'].items()}
sensors = namespace['createSensorList'](config)
namespace.update(sensorList=sensors, sensorListTmp=copy.deepcopy(sensors), element=element, datetime=FixedDatetime)
dispatch = {'barometer': 'readBaro', 'thermometer': 'readTemp', 'tank': 'readTank',
            'battery': 'readBatt', 'volt': 'readVolt', 'ohm': 'readOhm',
            'current': 'readCurrent', 'inclinometer': 'readIncline'}
for sid, meta in sensors.items():
    if meta['type'] in dispatch:
        namespace[dispatch[meta['type']]](sid, meta['pos'])
        if meta['type'] == 'battery':
            namespace['readBattNameVoltage'](sid, meta['pos'])
exec(compile(ast.Module(body=[output_assignment, output_loop], type_ignores=[]), '<original-output>', 'exec'), namespace)
json.dump({'sensorList': sensors, 'readings': namespace['sensorListTmp'], 'output': namespace['output']}, sys.stdout)
