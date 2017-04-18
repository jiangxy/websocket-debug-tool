package me.jxy.websocket.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.GenericMessage;

/**
 * channel interceptor
 *
 * @version 1.0
 * @author jiangxy
 */
public class ChannelInterceptor extends ChannelInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        logger.info("receive message " + command.name());

        // Interceptor可以做很多事，可以修改原来的消息，也可以通过MessageChannel发送更多消息
        // 常见的用法是权限认证

        Message<byte[]> newMessage = new GenericMessage<byte[]>("yoyoyo".getBytes(), message.getHeaders());
        return newMessage;
    }
}