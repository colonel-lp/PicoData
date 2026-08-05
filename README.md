#Code to pull data from the Simarine Pico over a WiFi network and publish it to an MQTT server or output as json.

Set up your MQTT server credentials in the mqtt file. 

Currently running on a Pi connected to the same network running mosquito as the mqtt server and nodeRed to collect and serve a webpage on the network with the data from the Simarine Pico and from an Electrodacus BMS.

Node Red configuration is also in my GitHub.

Core code is from https://github.com/htool/pico2signalk/

