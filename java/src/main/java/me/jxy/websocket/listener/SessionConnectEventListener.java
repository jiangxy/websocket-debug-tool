package me.jxy.websocket.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

/**
 * 监听websocket的连接事件
 *
 * @version 1.0
 * @author jiangxy
 */
@Component
public class SessionConnectEventListener implements ApplicationListener<SessionConnectedEvent> {

    // 有哪些事件可以监听：
    // https://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-stomp-appplication-context-events

    private static Logger logger = LoggerFactory.getLogger(SessionConnectEventListener.class);

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {
        Message<?> message = event.getMessage();
        logger.info("receive SessionConnectedEvent and headers = " + message.getHeaders());
    }

}
