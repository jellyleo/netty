package com.jellyleo.netty.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 
 * 功能描述:通道初始化
 *
 * @author Jelly
 * @created 2019年12月2日
 * @version 1.0.0
 */
public class JlChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel channel) {
		// 加入心跳处理
		channel.pipeline().addLast(new IdleStateHandler(10, 10, 10));
		channel.pipeline().addLast("http-codec", new HttpServerCodec());
		channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
		channel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
		// 在通道中添加业务处理实现
		channel.pipeline().addLast(new JlServerHandler());
	}

}
