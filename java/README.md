# A spring-based WebSocket server for test

With Spring 4.2.6.

## Quick Start

1. you need maven 3.
2. `git clone` and `mvn jetty:run` to start the embedded Jetty server.
3. open `http://localhost:8080/index.html` and enjoy.

## WebSocket URI

Once the Jetty server started, there are some URI available for WebSocket transport:

1. `ws://localhost:8080/handlerA` and `ws://localhost:8080/handlerB` for raw WebSocket.
2. `http://localhost:8080/sockjs/handlerA` and `http://localhost:8080/sockjs/handlerB` for SockJS.
3. `ws://localhost:8080/stomp` for raw WebSocket + STOMP.
4. `http://localhost:8080/stomp` for SockJS + STOMP.

For more detail, view [SpringMVC.xml](src/main/resources/springMVC.xml).
