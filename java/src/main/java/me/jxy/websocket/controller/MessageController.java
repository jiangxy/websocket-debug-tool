package me.jxy.websocket.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * 消息处理controller
 *
 * @version 1.0
 * @author jiangxy
 */
@Controller
public class MessageController {

    // spring的websocket处理遵循和springMVC类似的模式，将消息处理逻辑映射到controller对应的方法上
    // 首先必须理解spring中的消息流：https://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-stomp-message-flow

    // 关于各种注解和注入参数的说明：
    // https://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-stomp-handle-annotations

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Resource
    private SimpMessagingTemplate template; // 这个对象用来直接向broker发送消息

    /**
     * 发送到/app/handlerA的消息会被这个方法处理
     * 这个方法可以注入很多参数，参考官方文档
     * 如果这个方法有返回值，就会包装成Message对象发送到/topic/handlerA（这里topic前缀是写死的，跟配置无关）
     *
     * @return
     */
    @MessageMapping("/handlerA")
    public String handlerA(Message<byte[]> message, @Payload String payload) {
        logger.info("handlerA receive message: payload={}, headers={}", payload, message.getHeaders());

        // SimpMessagingTemplate只能向broker发送消息，所以下面这行代码其实是无效的，不会触发下面的handlerB方法
        template.convertAndSend("/app/handlerB", "message from handerA");

        // 这行代码才是有效的，会将消息向/topic/handlerB广播
        template.convertAndSend("/topic/handlerB", "message from handerA");
        return "OK";
    }

    /**
     * 发送到/app/handlerB的消息会被这个方法处理
     */
    @MessageMapping("/handlerB")
    public void handlerB(Message<byte[]> message, @Payload String payload) {
        logger.info("handlerB receive message: payload={}, headers={}", payload, message.getHeaders());
    }

    /**
     * 可以用SendTo注解设置返回的消息发到哪里
     */
    @MessageMapping("/handlerA/{id}") // 路径参数
    @SendTo("/queue/test")
    public String handlerA2(@DestinationVariable Integer id, // 路径参数
            MessageHeaders messageHeaders, MessageHeaderAccessor messageHeaderAccessor, SimpMessageHeaderAccessor simpMessageHeaderAccessor,
            StompHeaderAccessor stompHeaderAccessor) {
        logger.info("test SendTo, session = {}", simpMessageHeaderAccessor.getSessionId());
        // 可注入的参数非常多
        // MessageHeaders是一个map，最原始的header
        // MessageHeaderAccessor对MessageHeaders包装了下，新增了一些get/set方法
        // SimpMessageHeaderAccessor似乎是用于访问spring in-memory broker自己加的一些header，这些header都是simp前缀的
        // StompHeaderAccessor用于访问STOMP协议的一些header，在MessageHeaders中这被称为native header

        return "OK " + id;
    }

    /**
     * SubscribeMapping注解比较特殊，如果客户端订阅/app/fake，就会触发这个方法
     * 这个方法如果有返回值，会直接返回给客户端，不会发送到broker，可用于实现request-reply模式
     * 如果没有返回值，就什么都不会发生
     * 
     * 注意只能用于/app路径下，订阅/topic/fake不会触发这个方法
     */
    @SubscribeMapping("/fake")
    public String subscribeMapper() {
        logger.info("test SubscribeMapping");
        return "fake topic";
    }

}
