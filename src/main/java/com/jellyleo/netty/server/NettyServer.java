package com.jellyleo.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * 
 * 功能描述:netty服务
 *
 * @author Jelly
 * @created 2019年12月2日
 * @version 1.0.0
 */
@Slf4j
@Component
public class NettyServer {

	@Value("${netty.host}")
	private String host;
	@Value("${netty.port}")
	private int port;

	// 配置服务端NIO线程组
	private final EventLoopGroup parentGroup = new NioEventLoopGroup(); // NioEventLoopGroup extends
																		// MultithreadEventLoopGroup Math.max(1,
																		// SystemPropertyUtil.getInt("io.netty.eventLoopThreads",
																		// NettyRuntime.availableProcessors() * 2));
	private final EventLoopGroup childGroup = new NioEventLoopGroup();
	private Channel channel;

	public ChannelFuture bind() {
		ChannelFuture channelFuture = null;
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(parentGroup, childGroup).channel(NioServerSocketChannel.class) // 非阻塞模式
					.option(ChannelOption.SO_BACKLOG, 128).childHandler(new JlChannelInitializer());

			channelFuture = b.bind(new InetSocketAddress(host, port)).syncUninterruptibly();
			channel = channelFuture.channel();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (null != channelFuture && channelFuture.isSuccess()) {
				log.info("jellyleo netty server start done......");
			} else {
				log.error("jellyleo netty server start error......");
			}
		}
		return channelFuture;
	}

	public void destroy() {
		if (null == channel)
			return;
		channel.close();
		parentGroup.shutdownGracefully();
		childGroup.shutdownGracefully();
	}

	public Channel getChannel() {
		return channel;
	}

}
