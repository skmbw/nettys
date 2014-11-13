package com.vteba.protobuf.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import javax.inject.Inject;
import javax.inject.Named;

import com.vteba.netty.protobuf.HeartBeatHandler;
import com.vteba.protobuf.AddressBookProtos.AddressBook;

/**
 * Server Channel初始化器
 * @author yinlei
 * @since 2014-6-22
 */
@Named
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	@Inject
	private ServerHandler serverHandler;
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();// 管道线，相当于过滤器
		
		/******ChannelOutboundHandler（返回数据给调用端）向外发送数据，逆序执行******/
		// 2、加上头长度
//		pipeline.addLast("lengthPrepender", new LengthFieldPrepender(4));
		pipeline.addLast("protobufVarint32LengthFieldPrepender", new ProtobufVarint32LengthFieldPrepender());
		// 1、将字符转成字节
//		pipeline.addLast("encoder", new StringEncoder(Char.UTF8));
		pipeline.addLast("encoder", new ProtobufEncoder());
		
		// logger既是Inbound，又是Outbound
		pipeline.addLast("logger", new LoggingHandler(LogLevel.INFO));//子handler的日志，对一个socket连接的日志的记录
		
		/**********ChannelInboundHandler（接受数据），进来数据，顺序执行*************/
		
		// 1、获取去掉头长度的数据
		//pipeline.addLast("lengthFieldFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//		pipeline.addLast("lengthBasedFrameDecoder", new LengthBasedFrameDecoder(4, false, true));
		
		pipeline.addLast("protobufVarint32FrameDecoder", new ProtobufVarint32FrameDecoder());
		pipeline.addLast("protobufDecoder", new ProtobufDecoder(AddressBook.getDefaultInstance()));
		
		// 2、解码数据，转成字符串
//		pipeline.addLast("decoder", new StringDecoder(Char.UTF8));// 因为LengthBasedFrameDecoder已经解码了，所以不需要了
		
//		pipeline.addLast("ldleStateHandler", new IdleStateHandler(20, 10, 10));//双工的，既是Inbound又是Outbound，位置可以任意
		pipeline.addLast("heartBeatHandler", new HeartBeatHandler());// 进行心跳检测，如果是心跳消息，直接跳过下面的业务handler
		// 3、真正的业务处理器，一般放在最后（因为需要前面解码器的数据等），是ChannelInboundHandler
		pipeline.addLast("serverHandler", serverHandler);
	}

}
