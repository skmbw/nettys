package com.vteba.netty.server;

import javax.inject.Inject;
import javax.inject.Named;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动Netty Server守护进程
 * @author yinlei
 * @date 2014-6-22
 */
@Named
public class Server {

	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
	
	@Inject
	private ServerChannelInitializer serverChannelInitializer;
	
	public void start() {
		LOGGER.info("Netty Server守护进程启动。");
		EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();// 监听线程组，分派任务的主管
		EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();// 工作线程组，具体干活的员工
		
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)// 是否开启nagle算法，开启后会导致ack问题，true关闭
				.option(ChannelOption.SO_KEEPALIVE, true)// 这个不是长连接，每隔2H探测client是否死掉，对应用作用不是很大
				//输入连接指示（对连接的请求）的最大队列长度被设置为 backlog 参数。如果队列满时收到连接指示，则拒绝该连接。
				//backlog参数必须是大于 0 的正值。如果传递的值等于或小于 0，则假定为默认值。 
				//经过测试这个队列是按照FIFO（先进先出）的原则。
				//如果将accept这个函数放在一个循环体中时，backlog参数也不会有什么作用。或者简单的讲,运行ServerSocket的这个线程会阻塞时，无论是在accept，还是在read处阻塞，这个backlog参数才生效。
				.option(ChannelOption.SO_BACKLOG, 100)// 连接server请求的队列的缓冲区大小，socket被阻塞是才起作用
				.option(ChannelOption.SO_RCVBUF, 8192)// tcp接受数据缓冲区大小默认8192
				.option(ChannelOption.SO_SNDBUF, 8192)// tcp发送数据缓冲区大小默认8192
				.option(ChannelOption.SO_TIMEOUT, 5000)// 从socket中读取数据，超时时间，单位毫秒
				//启用/禁用具有指定逗留时间（以秒为单位）的 SO_LINGER。最大超时值是特定于平台的。 该设置仅影响套接字关闭。默认值为-1，表示禁用。
				//这个Socket选项可以影响close方法的行为。在默认情况下，当调用close方法后，将立即返回；如果这时仍然有未被送出的数据包，那么这些数据包将被丢弃。如果将linger参数设为一个正整数n时(n的值最大是65,535)，在调用close方法后，将最多被阻塞n秒。在这n秒内，系统将尽量将未送出的数据包发送出去；如果超过了n秒，如果还有未发送的数据包，这些数据包将全部被丢弃；而close方法会立即返回。如果将linger设为0，和关闭SO_LINGER选项的作用是一样的
				.option(ChannelOption.SO_LINGER, 0)//
				.handler(new LoggingHandler(LogLevel.INFO))// 父handler的日志，对连接的建立，断开等的记录，不涉及到具体的子handler的数据
				.childHandler(serverChannelInitializer);
			
			// 绑定监听的端口
			ChannelFuture future = serverBootstrap.bind(8080).sync();
			
			LOGGER.info("Netty Server守护进程启动成功。");
			
			// 等待直到链接被关闭
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			LOGGER.error("Netty Server守护进程启动异常。", e);
		} finally {
			// 关闭线程池，释放资源
			LOGGER.info("Netty Server关闭，释放资源。");
			bossEventLoopGroup.shutdownGracefully();
			workerEventLoopGroup.shutdownGracefully();
		}
	}

}
