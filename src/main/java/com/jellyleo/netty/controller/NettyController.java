package com.jellyleo.netty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * 功能描述:netty控制器
 *
 * @author Jelly
 * @created 2019年12月2日
 * @version 1.0.0
 */
@Controller
public class NettyController {

	@RequestMapping("/test")
	public String test() {
		return "success";
	}

}
