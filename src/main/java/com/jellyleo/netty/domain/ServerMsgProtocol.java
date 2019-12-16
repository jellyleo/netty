package com.jellyleo.netty.domain;

import lombok.Data;

/**
 * 
 * 功能描述:服务端消息
 *
 * @author Jelly
 * @created 2019年12月2日
 * @version 1.0.0
 */
@Data
public class ServerMsgProtocol {

	private int type; // 链接信息;1自发信息、2群发消息
	private String channelId; // 通信管道ID，实际使用中会映射成用户名
	private String userHeadImg; // 用户头像[模拟分配]
	private String msgInfo; // 通信消息
}
