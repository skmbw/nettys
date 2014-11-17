package com.vteba.protostuff;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vteba.utils.json.Node;

/**
 * 服务端Handler，业务处理器
 * @author yinlei
 * @since 2014-6-22
 */
@Named
@Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Node result = (Node) msg;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("接收到的消息=[{}].", result);
		}
		// 一般不要循环调用，循环一次会作为一次独立的调用发送给服务端
		
		Node node = new Node();
		node.setName("处理成功");
		ctx.write(node);// 返回信息给调用方（客户端）
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();// flush返回给调用者的信息
//		ctx.close();// 主动关闭链接
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("ServerHandler处理异常，", cause.getMessage());
		}
		ctx.close();// 出现异常关闭链接
	}
	
	
}
