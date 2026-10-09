'use strict';

// Loopback MQTT 3.1.1 wire fixture, not a production broker or Mosquitto test.
const net = require('node:net');
const { EventEmitter } = require('node:events');
const mqttPacket = require('mqtt-packet');

async function mqttBroker(t) {
  const events = new EventEmitter(), sockets = new Set(), subscriptions = new Map();
  let accepted = true, subscriptionAllowed = true, connections = 0;
  const server = net.createServer(socket => {
    connections++; sockets.add(socket); subscriptions.set(socket, new Set());
    socket.on('close', () => { sockets.delete(socket); subscriptions.delete(socket); }); socket.on('error', () => {});
    const parser = mqttPacket.parser({ protocolVersion: 4 });
    parser.on('error', () => socket.destroy());
    parser.on('packet', packet => {
      events.emit(packet.cmd, packet);
      if (packet.cmd === 'connect') socket.write(mqttPacket.generate({
        cmd: 'connack', returnCode: accepted ? 0 : 5, sessionPresent: false,
      }));
      if (packet.cmd === 'pingreq') socket.write(mqttPacket.generate({ cmd: 'pingresp' }));
      if (packet.cmd === 'subscribe') {
        if (subscriptionAllowed) for (const value of packet.subscriptions) subscriptions.get(socket).add(value.topic);
        socket.write(mqttPacket.generate({ cmd: 'suback', messageId: packet.messageId,
          granted: packet.subscriptions.map(() => subscriptionAllowed ? 0 : 128) }));
      }
      if (packet.cmd === 'unsubscribe') {
        for (const topic of packet.unsubscriptions) subscriptions.get(socket).delete(topic);
        socket.write(mqttPacket.generate({ cmd: 'unsuback', messageId: packet.messageId }));
      }
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
    deny() { accepted = false; }, allow() { accepted = true; },
    denySubscriptions() { subscriptionAllowed = false; }, allowSubscriptions() { subscriptionAllowed = true; },
    send(topic, payload, { retain = false } = {}) {
      const packet = mqttPacket.generate({ cmd: 'publish', topic,
        payload: Buffer.isBuffer(payload) ? payload : Buffer.from(JSON.stringify(payload)), qos: 0, retain });
      for (const socket of sockets) if (subscriptions.get(socket)?.has(topic)) socket.write(packet);
    } };
}
module.exports = { mqttBroker };
