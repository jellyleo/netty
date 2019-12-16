package com.jellyleo.netty.domain;

import lombok.Data;

/**
 * 
 * 功能描述:客户端消息
 *
 * @author Jelly
 * @created 2019年12月2日
 * @version 1.0.0
 */
@Data
public class ClientMsgProtocol {

	private int type; // 0.心跳检测 1.请求个人信息 2.发送聊天信息
	private String msgInfo; // 消息
}
