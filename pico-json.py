#!/usr/bin/env python3

import os
import time
import socket
import sys
import select
import requests
import json
import brainsmoke
import copy
import dictdiffer  # Ensure dictdiffer is installed
from datetime import datetime

responses = [''] * 200
sensors = ['']

def debug(string):
    if "DEBUG" in os.environ:
        if os.environ['DEBUG'] == 'pico':
            print(string)
            sys.stdout.flush()

def empty_socket(sock):
    """remove the data present on the socket"""
    input = [sock]
    while 1:
        inputready, o, e = select.select(input, [], [], 0.0)
        if len(inputready) == 0: break
        for s in inputready: s.recv(1)

def striplist(l):
    return([x.strip() for x in l])

def hexdump(b):
    hex = ' '.join(["%02x" % b])
    if (len(hex) == 3):
        hex = "0" + hex
    if (len(hex) == 2):
        hex = "00" + hex
    return hex[0:2] + " " + hex[2:4]

def HexToByte(hexStr):
    """
    Convert a string hex byte values into a byte string. The Hex Byte values may
    or may not be space separated.
    """
    bytes = []
    hexStr = ''.join(hexStr.split(" "))
    for i in range(0, len(hexStr), 2):
        bytes.append(chr(int(hexStr[i:i+2], 16)))
    return ''.join(bytes)

def ByteToHex(byteStr):
    """
    Convert a byte string to its hex string representation e.g. for output.
    """
    return ''.join(["%02X " % ord(x) for x in byteStr]).strip()

def HexToInt(hex, lastBytes):
    return int(hex.replace(' ', '')[-lastBytes:], 16)

def IntToDecimal(integer):
    return integer / float(10)

def BinToHex(message):
    response = ''
    for x in message:
        hexy = format(x, '02x')
        response = response + hexy + ' '
    return response

def parse(message):
    values = message.split(' ff')
    values = striplist(values)
    return values

def getNextField(response):
    field_nr = int(response[0:2], 16)
    field_type = int(response[3:5], 16)
    if (field_type == 1):
        data = response[6:17]
        response = response[21:]
        a = int(data[0:5].replace(' ', ''), 16)
        b = int(data[6:11].replace(' ', ''), 16)
        field_data = [a, b]
        return (field_nr, field_data, response)
    if (field_type == 3):
        data = response[21:32]
        response = response[36:]
        if (data[0:11] == '7f ff ff ff'):
            return field_nr, '', response
        else:
            a = int(data[0:5].replace(' ', ''), 16)
            b = int(data[6:11].replace(' ', ''), 16)
            field_data = [a, b]
            return field_nr, field_data, response
    if (field_type == 4):  # Text string
        response = response[21:]
        nextHex = response[0:2]
        word = ''
        while (nextHex != '00'):
            word += nextHex
            response = response[3:]
            nextHex = response[0:2]
        word = HexToByte(word)
        response = response[6:]  # Strip separator
        return field_nr, word, response
    debug("Unknown field type " + str(field_type))

def parseResponse(response):
    dict = {}
    response = response[42:]  # strip header
    while (len(response) > 6):
        field_nr, field_data, response = getNextField(response)
        dict[field_nr] = field_data
    return dict

def add_crc(message):
    fields = message.split()
    message_int = [int(x, 16) for x in fields[1:]]
    crc_int = brainsmoke.calc_rev_crc16(message_int[0:-1])
    return message + " " + hexdump(crc_int)

def send_receive(s, message):
    bytes = message.count(' ') + 1
    message = bytearray.fromhex(message)
    s.sendall(message)
    response = ''
    for x in s.recv(1024):
        hex = format(x, '02x')
        response = response + hex + ' '
    return response

def open_tcp(pico_ip, max_retries=5, retry_delay=5):
    serverport = 5001
    s = None
    retries = 0
    while retries < max_retries and not s:
        try:
            s = socket.create_connection((pico_ip, serverport), timeout=10)
            if s:
                debug(f"Connected to {pico_ip}:{serverport}")
                s.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
                return s  # Return the socket directly, do not rely on `with`
        except socket.error as e:
            debug(f"Connection attempt failed: {e}")
            s = None  # Ensure s is None if connection fails
        retries += 1
        if retries < max_retries:
            debug(f"Retrying in {retry_delay} seconds...")
            time.sleep(retry_delay)
    debug(f"Max retries ({max_retries}) reached.")
    return None

def get_pico_config(pico_ip):
    config = {}
    s = open_tcp(pico_ip)
    message = ('00 00 00 00 00 ff 02 04 8c 55 4b 00 03 ff')
    message = add_crc(message)
    response = send_receive(s, message)
    req_count = int(response.split()[19], 16) + 1

    for pos in range(req_count):
        message = ('00 00 00 00 00 ff 41 04 8c 55 4b 00 16 ff 00 01 00 00 00 ' + "%02x" % pos + ' ff 01 03 00 00 00 00 ff 00 00 00 00 ff')
        message = add_crc(message)
        response = send_receive(s, message)
        element = parseResponse(response)
        config[pos] = element

    s.close()  # Close tcp connection
    return config

def toTemperature(temp):
    if temp > 32768:
        temp = temp - 65536
    temp2 = float("%.2f" % (temp / 10.0))
    return temp2

def createSensorList(config):
    sensorList = {}
    fluid = ['Unknown', 'freshWater', 'fuel', 'wasteWater']
    fluid_type = ['Unknown', 'fresh water', 'diesel', 'blackwater']
    elementPos = 0
    for entry in config.keys():
        id = config[entry][0][1]
        type = config[entry][1][1]
        elementSize = 1
        sensorList[id] = {}
        if (type == 0):
            type = 'null'
            elementSize = 0
        if (type == 1):
            type = 'volt'
            sensorList[id].update({'name': config[entry][3]})
            if (config[entry][3] == 'PICO INTERNAL'):
                elementSize = 6
        if (type == 2):
            type = 'current'
            sensorList[id].update({'name': config[entry][3]})
            elementSize = 2
        if (type == 3):
            type = 'thermometer'
            sensorList[id].update({'name': config[entry][3]})
        if (type == 5):
            type = 'barometer'
            sensorList[id].update({'name': config[entry][3]})
            elementSize = 2
        if (type == 6):
            type = 'ohm'
            sensorList[id].update({'name': config[entry][3]})
        if (type == 8):
            type = 'tank'
            sensorList[id].update({'name': config[entry][3]})
            sensorList[id].update({'capacity': config[entry][7][1] / 10})
            sensorList[id].update({'fluid_type': fluid_type[config[entry][6][1]]})
            sensorList[id].update({'fluid': fluid[config[entry][6][1]]})
        if (type == 9):
            type = 'battery'
            sensorList[id].update({'name': config[entry][3]})
            sensorList[id].update({'capacity.nominal': config[entry][5][1] * 36 * 12})  # In Joule
            elementSize = 5
        if (type == 13):
            type = 'inclinometer'  # Corrected to use 'inclinometer'
            elementSize = 1

        sensorList[id].update({'type': type, 'pos': elementPos})
        elementPos = elementPos + elementSize
    return sensorList

debug( "Start UDP listener")
# Setup UDP broadcasting listener
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
client.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
client.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
client.bind(("", 43210))
message, addr = client.recvfrom(2048)  # Assign pico address
pico_ip = addr[0]
debug("See Pico at " + str(pico_ip))
config = get_pico_config(pico_ip)
debug("CONFIG:")
debug(config)

sensorList = createSensorList(config)
debug("SensorList:")
debug(sensorList)

responseB = [''] * 50
responseC = []
old_element = {}

def readBaro(sensorId, elementId):
    sensorListTmp[sensorId].update({'pressure': (element[elementId][1] + 65536) / 100})

def readTemp(sensorId, elementId):
    sensorListTmp[sensorId].update({'temperature': toTemperature(element[elementId][1])})

def readTank(sensorId, elementId):
    currentLevel = element[elementId][0] / float(1000)
    capacity = sensorList[sensorId].get('capacity', 0)
    remainingCapacity = element[elementId][1] / float(10)
    percentage = (remainingCapacity / capacity) * 100 if capacity else 0
    sensorListTmp[sensorId].update({'currentLevel': currentLevel})
    sensorListTmp[sensorId].update({'remainingCapacity': remainingCapacity})
    sensorListTmp[sensorId].update({'percentage': percentage})

def readBatt(sensorId, elementId):
    stateOfCharge = (element[elementId][0] / 16000.0)
    sensorListTmp[sensorId].update({'stateOfCharge': stateOfCharge})
    capacity = sensorList[sensorId].get('capacity.nominal', 0)
    capacity_remaining = (capacity * stateOfCharge / 43200)
    sensorListTmp[sensorId].update({'capacity.remaining': capacity_remaining})
    sensorListTmp[sensorId].update({'voltage': element[elementId + 2][1] / float(1000)})
    sensorListTmp[sensorId]['capacity.nominal'] = sensorListTmp[sensorId]['capacity.nominal'] / 43200
    current = element[elementId + 1][1]
    if (current > 25000):
        current = (65535 - current) / float(100)
    else:
        current = current / float(100) * -1
    sensorListTmp[sensorId].update({'current': -abs(current)})
    if (element[elementId][0] != 65535):
        timeRemaining = round(sensorList[sensorId]['capacity.nominal'] / 12 / ((current * stateOfCharge) + 0.001))
        if (timeRemaining < 0):
            timeRemaining = 60 * 60 * 24 * 7  # One week
        sensorListTmp[sensorId].update({'capacity.timeRemaining': timeRemaining})

def readBattNameVoltage(sensorId, elementId):
    voltage = element[elementId + 2][1] / float(1000)
    name = sensorList[sensorId].get('name')
    sensorListTmp[sensorId].update({'name': name, 'voltage': voltage, 'type': 'battery'})

def readVolt(sensorId, elementId):
    sensorListTmp[sensorId].update({'voltage': element[elementId][1] / float(1000)})

def readOhm(sensorId, elementId):
    sensorListTmp[sensorId].update({'ohm': element[elementId][1]})

def readCurrent(sensorId, elementId):
    current = element[elementId][1]
    if (current > 25000):
        current = (65535 - current) / float(100)
    else:
        current = current / float(100) * -1
    sensorListTmp[sensorId].update({'current': -abs(current)})

def readIncline(sensorId, elementId):
    inclinometer_type = 'pitch' if elementId % 2 == 0 else 'roll'
    sensorListTmp[sensorId].update({inclinometer_type: element[elementId][1] / 10.0})

while True:
    updates = []
    sensorListTmp = copy.deepcopy(sensorList)

    try:
        message, addr = client.recvfrom(2048)
        debug("Received packet with length " + str(len(message)))
    except socket.timeout:
        debug("Socket timeout, continuing to listen...")
        continue

    if len(message) > 100 and len(message) < 1000:
        response = BinToHex(message)
        debug("response: " + response)

        if response[18] == 'b':
            element = parseResponse(response)
            debug(element)
            for diff in list(dictdiffer.diff(old_element, element)):
                debug(diff)
            old_element = copy.deepcopy(element)

            for item in sensorList:
                elId = sensorList[item]['pos']
                itemType = sensorList[item]['type']
                if itemType == 'barometer':
                    readBaro(item, elId)
                elif itemType == 'thermometer':
                    readTemp(item, elId)
                elif itemType == 'battery':
                    readBatt(item, elId)
                    readBattNameVoltage(item, elId)
                elif itemType == 'ohm':
                    readOhm(item, elId)
                elif itemType == 'volt':
                    readVolt(item, elId)
                elif itemType == 'current':
                    readCurrent(item, elId)
                elif itemType == 'tank':
                    readTank(item, elId)
                elif itemType == 'inclinometer':
                    readIncline(item, elId)

            output = {
                "time": {
                    "year": datetime.now().year % 100,  # Last two digits of the year
                    "month": datetime.now().month,
                    "day": datetime.now().day,
                    "hour": datetime.now().hour,
                    "minute": datetime.now().minute,
                    "second": datetime.now().second
                },
                "barometer": {},
                "inclinometer": {"pitch": None, "roll": None},
                "voltage": {},
                "current": {},
                "temperature": {},
                "tank": {},
                "battery": {}
            }

            for sensorId, sensorData in sensorListTmp.items():
                name = sensorData.get('name')
                values = {
                    "voltage": sensorData.get('voltage'),
                    "pressure": sensorData.get('pressure'),
                    "temperature": sensorData.get('temperature'),
                    "current": sensorData.get('current'),
                    "capacity_remaining": sensorData.get('capacity.remaining'),
                    "currentLevel": sensorData.get('currentLevel'),
                    "remainingCapacity": sensorData.get('remainingCapacity'),
                    "percentage": sensorData.get('percentage'),
                    "pitch": sensorData.get('pitch'),
                    "roll": sensorData.get('roll')
                }
                filtered_values = {key: value for key, value in values.items() if value is not None}
                if name and filtered_values and '[' not in name:
                    if sensorData['type'] == 'barometer':
                        output["barometer"] = filtered_values["pressure"]
                    elif sensorData['type'] == 'inclinometer':
                        if "pitch" in filtered_values:
                            output["inclinometer"]["pitch"] = filtered_values["pitch"]
                        if "roll" in filtered_values:
                            output["inclinometer"]["roll"] = filtered_values["roll"]
                    elif sensorData['type'] == 'volt':
                        output["voltage"][name] = filtered_values["voltage"]
                    elif sensorData['type'] == 'current':
                        output["current"][name] = filtered_values["current"]
                    elif sensorData['type'] == 'thermometer':
                        output["temperature"][name] = filtered_values["temperature"]
                    elif sensorData['type'] == 'tank':
                        output["tank"][name] = {
                            "capacity_nominal": sensorData.get('capacity'),
                            "capacity_remaining": filtered_values.get('remainingCapacity'),
                            "percentage": int(round(filtered_values.get('percentage', 0)))
                        }
             
                    elif sensorData['type'] == 'battery':
                        output["battery"][name] = {
                            "capacity_nominal": sensorData.get('capacity.nominal'),
                            #"capacity_remaining": int(round(filtered_values.get('capacity_remaining', 5))),
                            "capacity_remaining": filtered_values.get('capacity_remaining'),
                            "state_of_charge": sensorData.get('stateOfCharge'),
                            "current": filtered_values.get('current'),
                            "voltage": filtered_values.get('voltage')
                        }
                        output["voltage"][name] = filtered_values["voltage"]

            
            print(json.dumps(output, separators=(',', ':')))  # Remove spaces between values and strings

            sys.stdout.flush()
            time.sleep(0.9)
            empty_socket(client)
