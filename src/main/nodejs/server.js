/*
 * Copyright (C) 2011 Universität zu Lübeck, Institut für Telematik (ITM)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var HOST = '0.0.0.0';
var PORT = 8080;

var socket = require('socket.io');
var express = require('express');

var app = express.createServer();
app.use(express.static(__dirname ));
app.listen(PORT, HOST);
console.log("Express server listening on port %d", app.address().port);

var io = socket.listen(app);
var redis = require("redis");
var db = redis.createClient();

var socketClient = null;

io.sockets.on('connection', function(client) {
    console.log(client.sessionId + " connected");
    socketClient = client;

    client.on('disconnect', function() {
        //db.quit();
    });
});

db.on("error", function (err) {
    console.log("Error " + err);
});

db.subscribe("sensor_readings");

db.on("message", function (channel, msg) {
    console.log(msg);
    if (socketClient) {
        socketClient.send(msg);
    }
});








