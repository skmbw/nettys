package com.vteba.netty.protobuf;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vteba.protobuf.AddressBookProtos.AddressBook;
import com.vteba.protobuf.AddressBookProtos.MessageType;

/**
 * 心跳处理器。客户端和服务端都需要配置一个心跳处理器。
 * <p>配合io.netty.handler.timeout.IdleStateHandler，IdleStateHandler一般配置在客户端。
 * 让客户端主动发起ping检测，Server端响应pong。IdleStateHandler配置在Server端亦可，但是会增加Server端负载。
 * @author yinlei
 * @date 2014-11-9
 */
public class HeartBeatHandler extends ChannelDuplexHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatHandler.class);
	
	public static final String PING = "ping";
	public static final String PONG = "pong";
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof String) {
			String heart = (String) msg;
			if (heart.equals(PING)) {// Server接收到Client的心跳ping消息
				ctx.write(PONG);
			} else if (heart.equals(PONG)) {// Client接收到Server的心跳pong消息
				// pong消息不处理，等待Netty核心的事件通知，空闲时才发送ping消息，减少负载
			} else {
				super.channelRead(ctx, msg);// 不是心跳信息，向下传递消息，给下面的handler处理
			}
		} else {
			AddressBook addressBook = (AddressBook) msg;
			if (addressBook.getType() == MessageType.PING) {
				AddressBook.Builder builder = AddressBook.newBuilder();
				builder.setType(MessageType.PONG);
				ctx.write(builder.build());
			} else if (addressBook.getType() == MessageType.PONG) {
				// 不作处理，等待netty的核心事件通知
			} else {
				super.channelRead(ctx, msg);
			}
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				ctx.close();// 读超时关闭
			} else if (event.state() == IdleState.ALL_IDLE) {
				AddressBook.Builder builder = AddressBook.newBuilder();
				builder.setType(MessageType.PING);
				ctx.write(builder.build());
				ctx.flush();
			}
		}
		super.userEventTriggered(ctx, evt);// 将事件向下传递
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		LOGGER.error("心跳连接异常。", cause.getMessage());
		ctx.fireExceptionCaught(cause);
	}

}
