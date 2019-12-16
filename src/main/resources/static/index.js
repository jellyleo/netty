// JavaScript Document
var socket;
$(function () {
	var lockReconnect = false;//避免重复连接
	var wsUrl = "ws://118.25.46.151:7397/websocket";

	if (!window.WebSocket) {
		window.WebSocket = window.MozWebSocket;
	}

	if (!window.WebSocket) {
		alert("您的浏览器不支持WebSocket协议！推荐使用谷歌浏览器进行测试。");
		return;
	}

	function createWebSocket(url) {
		try {
			socket = new WebSocket(url);
			initEventHandle();
		} catch (error) {
			reconnect(wsUrl);
		}
	}

	function reconnect(url) {
		if (lockReconnect) return;
		var recto;
		lockReconnect = true;
		//没连接上会一直重连，设置延迟避免请求过多
		clearTimeout(recto);
		recto = setTimeout(function () {
			createWebSocket(wsUrl);
			console.log("正在重连，当前时间" + new Date())
			lockReconnect = false;
		}, 3000); //这里设置重连间隔(ms)
	}

	function initEventHandle() {
		// 收到服务器消息后响应
		socket.onmessage = function (event) {
			//心跳检测重置
			heartCheck.reset();

			var msg = JSON.parse(event.data);
			//链接信息;1自发信息、2群发消息
			if (1 == msg.type) {
				jQuery.data(document.body, 'channelId', msg.channelId);
				return;
			}

			//链接信息;1自发信息、2群发消息
			if (2 == msg.type) {

				var channelId = msg.channelId;
				//自己
				if (channelId == jQuery.data(document.body, 'channelId')) {
					var module = $(".msgBlockOwnerClone").clone();
					module.removeClass("msgBlockOwnerClone").addClass("msgBlockOwner").css({ display: "block" });
					module.find(".headPoint").attr("src", "img/" + msg.userHeadImg);
					module.find(".msgBlock_msgInfo .msgPoint").text(msg.msgInfo);

					$("#msgPoint").before(module);

					util.divScroll();
				}
				//好友
				else {
					var module = $(".msgBlockFriendClone").clone();
					module.removeClass("msgBlockFriendClone").addClass("msgBlockFriend").css({ display: "block" });
					module.find(".headPoint").attr("src", "img/" + msg.userHeadImg);
					module.find(".msgBlock_channelId").text("通道ID：" + msg.channelId);
					module.find(".msgBlock_msgInfo .msgPoint").text(msg.msgInfo);
					$("#msgPoint").before(module);
					util.divScroll();
				}

			}

		};

		// 连接成功建立后响应
		socket.onopen = function (event) {
			console.info("打开WebSoket 服务正常，浏览器支持WebSoket!");
			//心跳检测重置
			heartCheck.reset();
			var clientMsgProtocol = {};
			clientMsgProtocol.type = 1;
			clientMsgProtocol.msgInfo = "请求个人信息";
			socket.send(JSON.stringify(clientMsgProtocol));
		};
		// 连接关闭后响应
		socket.onclose = function (event) {
			console.info("WebSocket 关闭");
			heartCheck.clear();
			reconnect(wsUrl);//重连
		};
		socket.onerror = function () {
			heartCheck.clear();
			reconnect(wsUrl);//重连
		};
	}

	//心跳检测
	var heartCheck = {
		intervalId: null,
		timeout: 9000,
		reset: function () {
			clearInterval(this.intervalId);
			this.intervalId = setInterval(this.send, this.timeout);
		},
		clear: function () {
			clearInterval(this.intervalId);
		},
		send: function () {
			if (socket) {
				var clientMsgProtocol = {};
				clientMsgProtocol.type = 0;
				clientMsgProtocol.msgInfo = "";
				socket.send(JSON.stringify(clientMsgProtocol));
				console.log("心跳");
			} else {
				reconnect(wsUrl);
			}
		}
	}

	// 强制退出
	window.onunload = function () {
		socket.close();
	}
	createWebSocket(wsUrl);/**启动连接**/

	document.onkeydown = function (e) {
		//console.log(e.ctrlKey);
		if (13 == e.keyCode) {
			//console.log(c1);
			util.send();
		}
	}

	/*+++++ 拖曳效果 ++++++
	 *原理：标记拖曳状态dragging ,坐标位置iX, iY
	 *         mousedown:fn(){dragging = true, 记录起始坐标位置，设置鼠标捕获}
	 *         mouseover:fn(){判断如果dragging = true, 则当前坐标位置 - 记录起始坐标位置，绝对定位的元素获得差值}
	 *         mouseup:fn(){dragging = false, 释放鼠标捕获，防止冒泡}
	 */
	var dragging = false;
	var iX, iY;

	$("#leftDiv").mousedown(function (e) {
		dragging = true;
		iX = e.clientX - this.offsetLeft;
		iY = e.clientY - this.offsetTop;
		this.setCapture && this.setCapture();
		return false;
	});

	document.onmousemove = function (e) {
		if (dragging) {
			var e = e || window.event;
			var oX = e.clientX - iX;
			var oY = e.clientY - iY;
			$("#chatDiv").css({
				"left": oX + "px",
				"top": oY + "px"
			});
			return false;
		}
	};

	$(document).mouseup(function (e) {
		dragging = false;
		$("#chatDiv")[0].releaseCapture;
		e.cancelBubble = true;
	})

});

util = {
	send: function () {
		if (!window.WebSocket) { return; }
		if (socket.readyState == WebSocket.OPEN) {
			var clientMsgProtocol = {};
			clientMsgProtocol.type = 2;
			clientMsgProtocol.msgInfo = $("#sendBox").val();
			socket.send(JSON.stringify(clientMsgProtocol));
			$("#sendBox").val("");
		} else {
			alert("WebSocket 连接没有建立成功！");
		}
	},
	divScroll: function () {
		var div = document.getElementById('show');
		div.scrollTop = div.scrollHeight;
	}
};