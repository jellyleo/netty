package com.jellyleo.netty.server;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 
 * 功能描述:
 *
 * @author Jelly
 * @created 2019年12月2日
 * @version 1.0.0
 */
public class ChannelHandler {

	// 用于存放用户Channel信息，也可以建立map结构模拟不同的消息群
	public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

}
