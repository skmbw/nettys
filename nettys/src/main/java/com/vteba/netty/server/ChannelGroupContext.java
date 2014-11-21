package com.vteba.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

import javax.inject.Named;

/**
 * 持有所有的客户端连接Channel，用于向client push消息，由server主动发起。
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
	
	/**
	 * 保存Channel（和客户端之间的连接通道）到ChannelGroup中。一般是新建连接时调用
	 * @param channel 待保存的channel
	 */
	public void addChannel(Channel channel) {
		channelGroup.add(channel);
	}
	
	/**
	 * 删除指定的Channel，断开连接时调用
	 * @param channel 待删除的channel
	 */
	public void removeChannel(Channel channel) {
		channelGroup.remove(channel);
	}
	
	/**
	 * Broadcast a message to multiple Channels
	 * @param message 要广播的消息
	 */
	public void send(Object message) {
		channelGroup.write(message);
	}
}
