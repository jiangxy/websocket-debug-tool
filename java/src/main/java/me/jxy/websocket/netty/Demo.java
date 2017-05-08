package me.jxy.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 一个简单的websocket服务端
 *
 * @version 1.0
 * @author jiangxy
 */
public class Demo {

    // netty官方的例子：http://netty.io/4.1/xref/io/netty/example/http/websocketx/server/package-summary.html
    // 更完善，但是也更复杂

    public static void main(String[] args) {

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(boss, worker).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChildChannelHandler());

            ChannelFuture f = server.bind(12345).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private static class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            // 前面这一堆handler和普通的http处理是一样的，都是netty直接提供的handler
            pipeline.addLast("http-decoder", new HttpRequestDecoder());
            pipeline.addLast("http-aggr", new HttpObjectAggregator(65535));
            pipeline.addLast("http-encoder", new HttpResponseEncoder());
            pipeline.addLast("http-chunked", new ChunkedWriteHandler());

            // 这里才是真正的处理逻辑
            pipeline.addLast("handler", new WebsocketHandler());
        }

    }

    private static class WebsocketHandler extends ChannelInboundHandlerAdapter {

        private WebSocketServerHandshaker handshaker;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 判断是否是握手消息，握手是基于http的
            if (msg instanceof FullHttpRequest) {
                handleHttp(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                handleWebSocket(ctx, (WebSocketFrame) msg);
            } else {
                sendError(ctx);
            }
        }

        private void sendError(ChannelHandlerContext ctx) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private void handleHttp(ChannelHandlerContext ctx, FullHttpRequest msg) {
            if (!msg.decoderResult().isSuccess() || !"websocket".equals(msg.headers().get("Upgrade"))) {
                sendError(ctx);
                return;
            }

            // http握手
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:12345/websocket", null, false);
            handshaker = wsFactory.newHandshaker(msg);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), msg);
            }
        }

        // 真正去处理websocket消息
        private void handleWebSocket(ChannelHandlerContext ctx, WebSocketFrame msg) {
            if (msg instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg.retain());
                return;
            }

            if (msg instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(msg.content().retain()));
                return;
            }

            if (!(msg instanceof TextWebSocketFrame)) {
                ctx.channel().write(new TextWebSocketFrame("only support text message"));
                return;
            }

            TextWebSocketFrame frame = (TextWebSocketFrame) msg;
            String request = frame.text();
            System.out.println("server receive " + request);

            // 类似EchoServer，注意flush
            ctx.channel().writeAndFlush(new TextWebSocketFrame("server recevie " + request));
        }
    }

}
