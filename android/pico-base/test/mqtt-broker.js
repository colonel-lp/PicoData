'use strict';

// Loopback MQTT 3.1.1 wire fixture, not a production broker or Mosquitto test.
const net = require('node:net');
const { EventEmitter } = require('node:events');
const mqttPacket = require('mqtt-packet');

async function mqttBroker(t) {
  const events = new EventEmitter(), sockets = new Set();
  let accepted = true, connections = 0;
  const server = net.createServer(socket => {
    connections++; sockets.add(socket);
    socket.on('close', () => sockets.delete(socket)); socket.on('error', () => {});
    const parser = mqttPacket.parser({ protocolVersion: 4 });
    parser.on('error', () => socket.destroy());
    parser.on('packet', packet => {
      events.emit(packet.cmd, packet);
      if (packet.cmd === 'connect') socket.write(mqttPacket.generate({
        cmd: 'connack', returnCode: accepted ? 0 : 5, sessionPresent: false,
      }));
      if (packet.cmd === 'pingreq') socket.write(mqttPacket.generate({ cmd: 'pingresp' }));
      if (packet.cmd === 'disconnect') socket.end();
    });
    socket.on('data', buffer => parser.parse(buffer));
  });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  t.after(async () => {
    for (const socket of sockets) socket.destroy();
    await new Promise(resolve => server.close(resolve));
  });
  return { events, port: server.address().port, get connections() { return connections; },
    drop() { for (const socket of sockets) socket.destroy(); },
    deny() { accepted = false; }, allow() { accepted = true; } };
}
module.exports = { mqttBroker };
