package com.jellyleo.netty.server;

import com.alibaba.fastjson.JSON;
import com.jellyleo.netty.domain.ClientMsgProtocol;
import com.jellyleo.netty.util.MsgUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 功能描述:netty业务处理器
 *
 * @author Jelly
 * @created 2019年12月2日
 * @version 1.0.0
 */
@Slf4j
public class JlServerHandler extends ChannelInboundHandlerAdapter {

	private WebSocketServerHandshaker handshaker;

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.ALL_IDLE) {
				log.info("Warning Warning... Reader And Writer Idle... Channel Closing...");
				ctx.close();
			}
		}
		ctx.flush();
	}

	/**
	 * 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SocketChannel channel = (SocketChannel) ctx.channel();
		log.info("链接报告开始");
		log.info("链接报告信息：有一客户端链接到本服务端");
		log.info("链接报告IP:{}", channel.localAddress().getHostString());
		log.info("链接报告Port:{}", channel.localAddress().getPort());
		log.info("链接报告完毕");
		ChannelHandler.channelGroup.add(ctx.channel());
	}

	/**
	 * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("客户端断开链接{}", ctx.channel().localAddress().toString());
		ChannelHandler.channelGroup.remove(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		// http
		if (msg instanceof FullHttpRequest) {

			FullHttpRequest httpRequest = (FullHttpRequest) msg;

			if (!httpRequest.decoderResult().isSuccess()) {

				DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.BAD_REQUEST);

				// 返回应答给客户端
				if (httpResponse.status().code() != 200) {
					ByteBuf buf = Unpooled.copiedBuffer(httpResponse.status().toString(), CharsetUtil.UTF_8);
					httpResponse.content().writeBytes(buf);
					buf.release();
				}

				// 如果是非Keep-Alive，关闭连接
				ChannelFuture f = ctx.channel().writeAndFlush(httpResponse);
				if (httpResponse.status().code() != 200) {
					f.addListener(ChannelFutureListener.CLOSE);
				}

				return;
			}

			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
					"ws:/" + ctx.channel() + "/websocket", null, false);
			handshaker = wsFactory.newHandshaker(httpRequest);

			if (null == handshaker) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
			} else {
				handshaker.handshake(ctx.channel(), httpRequest);
			}

			return;
		}

		// ws
		if (msg instanceof WebSocketFrame) {

			WebSocketFrame webSocketFrame = (WebSocketFrame) msg;

			// 关闭请求
			if (webSocketFrame instanceof CloseWebSocketFrame) {
				handshaker.close(ctx.channel(), (CloseWebSocketFrame) webSocketFrame.retain());
				return;
			}

			// ping请求
			if (webSocketFrame instanceof PingWebSocketFrame) {
				ctx.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
				return;
			}

			// 只支持文本格式，不支持二进制消息
			if (!(webSocketFrame instanceof TextWebSocketFrame)) {
				throw new Exception("仅支持文本格式");
			}

			String request = ((TextWebSocketFrame) webSocketFrame).text();
			log.info("客户端 [" + ctx.channel().id() + "] 发来消息：" + request);

			ClientMsgProtocol clientMsgProtocol = JSON.parseObject(request, ClientMsgProtocol.class);
			// 心跳检测
			if (0 == clientMsgProtocol.getType()) {
				ctx.flush();
				return;
			}
			// 请求个人信息
			if (1 == clientMsgProtocol.getType()) {
				ctx.channel().writeAndFlush(MsgUtil.buildMsgOwner(ctx.channel().id().toString()));
				return;
			}
			// 群发消息
			if (2 == clientMsgProtocol.getType()) {
				TextWebSocketFrame textWebSocketFrame = MsgUtil.buildMsgAll(ctx.channel().id().toString(),
						clientMsgProtocol.getMsgInfo());
				ChannelHandler.channelGroup.writeAndFlush(textWebSocketFrame);
			}

		}

	}

	/**
	 * 捕获异常，当发生异常的时候，可以做一些相应的处理，比如打印日志、关闭链接
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		log.info("异常信息：\r\n" + cause.getMessage());
	}

}
