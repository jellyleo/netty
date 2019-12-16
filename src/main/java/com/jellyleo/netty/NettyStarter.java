package com.jellyleo.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.jellyleo.netty.server.NettyServer;

import io.netty.channel.ChannelFuture;

/**
 * 功能描述:netty服务启动类
 *
 * @author Jelly
 * @created 2019年12月2日
 * @version 1.0.0
 */
@Component
public class NettyStarter implements ApplicationRunner {

	@Autowired
	private NettyServer nettyServer;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		ChannelFuture channelFuture = nettyServer.bind();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> nettyServer.destroy()));
		channelFuture.channel().closeFuture().syncUninterruptibly();
	}

}
