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
$(document).ready(function() {

    var server = io.connect("http://localhost:8080");
    server.on("connect", function() {
        console.log("connected");
    });

    var sensorNodes = {};

    var charts = {};
    var series = {};

    var seriesOptions  = {};
    seriesOptions.temp = { strokeStyle: 'rgba(255, 0, 0, 1)', fillStyle: 'rgba(255, 0, 0, 0.1)', lineWidth: 3 };
    seriesOptions.hum  = { strokeStyle: 'rgba(0, 255, 0, 1)', fillStyle: 'rgba(0, 255, 0, 0.1)', lineWidth: 3 };
    seriesOptions.lum  = { strokeStyle: 'rgba(0, 0, 255, 1)', fillStyle: 'rgba(0, 0, 255, 0.1)', lineWidth: 3 };
    seriesOptions.irda = { strokeStyle: 'rgba(255, 255, 0, 1)', fillStyle: 'rgba(255, 255, 0, 0.1)', lineWidth: 3 };

    var delay = 1000; // 1 second
    var lastReading = {};
    var lastUpdated = {};
    var createCanvas = function(id) {
            var hexId = "0x" + Number(id).toString(16);
            return "<div>" + "<span>Sensor Node [" + hexId + "/" + id + "]</span><br/>" + "<canvas id=\"" + id + "\" width=\"800\" height=\"100\"></canvas>" + "</div>";
        };

    var createChart = function (senderId, seriesId) {
        $("#out").append(createCanvas(senderId));

        console.log("Just added new node: " + seriesId);
        console.log("There are now " + sensorNodes.length + " nodes.");

        charts[senderId] = new SmoothieChart({
            millisPerPixel: 20,
            grid: { strokeStyle: '#555555', lineWidth: 1, millisPerLine: 1000, verticalSections: 4 }
        });
        charts[senderId].streamTo(document.getElementById(senderId), delay);
    };

    var createDataSeries = function(senderId, seriesId, readingType) {
        // Add type to node meta data list
        if (sensorNodes[senderId]) {
            sensorNodes[senderId].push(readingType);
            console.log("sensorNodes[ " + senderId + " ].push( " + readingType + " )");
        }

        // Create new data series for data type "readingType"
        series[seriesId] = new TimeSeries();

        // Add data series to corresponding chart
        if (charts[senderId] && series[seriesId] && seriesOptions[readingType]) {
            charts[senderId].addTimeSeries(series[seriesId], seriesOptions[readingType]);
        }
    };

    var updateData = function() {
        for (var senderId in sensorNodes) {
            if (charts[senderId]) {
                for (var i = 0; i < sensorNodes[senderId].length; i++) {
                    var seriesId = senderId + "#" + sensorNodes[senderId][i];
                    if (series[seriesId]) {
                        series[seriesId].append(new Date().getTime(), lastReading[seriesId]);
                    }
                }
            }
        }
    };

    // Update data every interval specified by the value in "delay"
    setInterval(updateData, delay);

    // On message received from NodeJS server
    server.on('message', function (msg) {
        // Parse incoming message
        var message = JSON.parse(msg);

        lastUpdated = new Date();
        console.log("Timestamp: " + message.timestamp);
        lastUpdated.setTime(message.timestamp);

        // Collect sensor node meta data
        var senderId = message.sender;
        if (!sensorNodes[senderId]) {
            sensorNodes[senderId] = [];
        }

        // Create new chart for this sensor node
        if (!charts[senderId]) {
            createChart(senderId, seriesId);
        }

        // Create a data series for each type of data
        var seriesId = message.sender + "#" + message.readingType;
        if (!series[seriesId]) {
            createDataSeries(senderId, seriesId, message.readingType);
        }

        // Update data
        var parsedReading = parseFloat(message.reading);
        if (charts[senderId] && series[seriesId] && !isNaN(parsedReading)) {
            console.log("Received data: " + parsedReading);
            lastReading[seriesId] = parsedReading;
            $("#lastUpdated").text("Last updated: " + lastUpdated);
        }
    });

});
