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
public class HandlerA extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(HandlerA.class);

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 握手过程中很重要的一个事就是将http session复制过来
        // 注意无论sockjs还是raw websocket，url中都是可以带参数的，spring似乎会对token参数做一些特殊处理
        logger.info("HandlerA received message: {}, url = {}, attrMap = {}", message.getPayload(), session.getUri().toString(), session.getAttributes());
        TextMessage resMsg = new TextMessage("[HandlerA] I received: " + message.getPayload());
        session.sendMessage(resMsg);
    }
}
