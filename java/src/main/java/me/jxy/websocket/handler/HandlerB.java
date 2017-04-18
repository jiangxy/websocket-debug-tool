package me.jxy.websocket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 测试用的handler
 *
 * @version 1.0
 * @author jiangxy
 */
public class HandlerB extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(HandlerB.class);

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("HandlerB received message: {}", message.getPayload());
        TextMessage resMsg = new TextMessage("[HandlerB] I received: " + message.getPayload());
        session.sendMessage(resMsg);
    }
}