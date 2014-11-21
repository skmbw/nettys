package com.vteba.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

import javax.inject.Named;

/**
 * 持有所有的客户端连接，用于向client push消息，由server主动发起。
 * 
 * @author yinlei
 * @see
 * @since 2014年11月21日 下午1:25:39
 */
@Named
public class ChannelGroupContext {
	private ChannelGroup channelGroup;
	
	public ChannelGroupContext() {
		// 对于大并发，后面的事件执行器要重写，因为他是单线程的，处理能力不够
		channelGroup = new DefaultChannelGroup("pushChannelGroup", PushEventExecutor.INSTANCE);
	}
	
	public void addChannel(Channel channel) {
		channelGroup.add(channel);
	}
	
	public void removeChannel(Channel channel) {
		channelGroup.remove(channel);
	}
	
	public void send(Object message) {
		channelGroup.write(message);
	}
}
