# WebSocket server for test

## Spring-based

Based on Spring 4.2.6

### Quick Start

1. you need maven 3.
2. `git clone` and `mvn jetty:run` to start the embedded Jetty server.
3. open `http://localhost:8080/index.html` and enjoy.

### WebSocket URI

Once the Jetty server started, there are some URI available for WebSocket transport:

1. `ws://localhost:8080/handlerA` and `ws://localhost:8080/handlerB` for raw WebSocket.
2. `http://localhost:8080/sockjs/handlerA` and `http://localhost:8080/sockjs/handlerB` for SockJS.
3. `http://localhost:8080/stomp` for SockJS + STOMP.

For more detail, view [SpringMVC.xml](src/main/resources/springMVC.xml).

## Netty-based

Based on Netty 4.1.9.Final

### Quick Start

1. you need maven 3.
2. `git clone` and `mvn jetty:run`  
3. open a new terminal window and `mvn exec:java -Dexec.mainClass=me.jxy.websocket.netty.Demo`
4. open `http://localhost:8080/index.html`

### WebSocket URI

This server is very simple, just a demo. Only one URI `ws://localhost:12345/websocket` is available for raw WebSocket.
