package me.jxy.websocket.handler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * 测试握手过程
 *
 * @version 1.0
 * @author jiangxy
 */
public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private static Logger logger = LoggerFactory.getLogger(HandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes)
            throws Exception {
        logger.info("before websocket handshake, request method={}, url={}, headers={}, attributes={}", request.getMethod(), request.getURI(),
                request.getHeaders(), attributes);
        // 这个方法如果返回false，说明握手失败
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        logger.info("after handshake");
        super.afterHandshake(request, response, wsHandler, ex);
    }

}
