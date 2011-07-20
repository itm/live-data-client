Live Data Client
================

Live Data Client (LDC) is a client for the [Wisebed Testbed][wisebed], which
displays sensor readings live in the web.  

When a program is deployed on the testbed that periodically sends sensor 
readings, the LDC fetches these readings utilizing the
[Testbed Runtime's][testbed-runtime] client utilies, publishes them on a channel 
using [Redis][redis]'s publish/subscribe mechanism. 

To push the data to the browser a [NodeJS][nodjs] server in conjunction with
the [Socket IO][socketio] library is used. The NodeJS server uses a Redis
client to subscribe to the sensor readings channel and pushes the incoming
data to the browser with Socket IO websockets.

The browser client uses the [Smoothie][smoothie] chart library to display the
live data. A chart is drawn for every sensor node's incoming data stream.


[socketio_github]:https://github.com/learnboost/socket.io
[socketio]:http://socket.io/
[nodejs_github]:https://github.com/joyent/node
[nodejs]:http://nodejs.org/
[node_redis_github]:https://github.com/mranney/node_redis
[redis]:http://redis.io/
[smoothie]:http://smoothiecharts.org/

[wiseui-beta]:http://wisebed.itm.uni-luebeck.de/wiseui-beta/
[wisebed]:http://wisebed.eu/

[testbed-runtime]:https://github.com/itm/testbed-runtime/tree/master/clients/scripting-client
