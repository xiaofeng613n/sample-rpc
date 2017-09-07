package com.rpc.server;

import com.alibaba.fastjson.JSONObject;
import com.rpc.client.ServiceProxyFactory;
import com.rpc.common.Constants;
import com.rpc.util.MyThreadFactory;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xiao on 2017/8/27.
 */
//@Data
public class ServerBootstrap implements ApplicationContextAware
{
	private ApplicationContext context;

	private ZkClient zkClient;
	private int workerThreads = 4;
	private int bizThreads = 8;

	private ServiceProxyFactory proxyFactory;
	private List<Exporter> exporters;

	private EventLoopGroup bossThreadPool;
	private EventLoopGroup workerThreadPool;

	private ExecutorService bizThreadPool;
	private io.netty.bootstrap.ServerBootstrap bootstrap;
	private ChannelInitializer<Channel> httpServerInitializer;
	private ChannelFuture channelFuture;
	private ServiceHandler serviceHandler;

	public void setPort(int port)
	{
		this.port = port;
	}

	private int port;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		this.context = applicationContext;
	}

	public void init()
	{
		reportInstance();
		initNettyServer();
	}

	public List<Exporter> getExporters()
	{
		if (this.exporters == null)
		{
			this.exporters = new ArrayList<>();
			String[] exportNames = context.getBeanNamesForType(Exporter.class);
			for (String exportName : exportNames)
			{
				Exporter exporter = (Exporter) context.getBean(exportName);
				this.exporters.add(exporter);
			}
		}
		return this.exporters;
	}

	private void initNettyServer()
	{
		System.out.println("################# initNettyServer() ... start! #################");

		bizThreadPool = Executors.newFixedThreadPool(bizThreads, new MyThreadFactory("biz"));

		bossThreadPool = new NioEventLoopGroup(1, new MyThreadFactory("boss"));

		workerThreadPool  = new NioEventLoopGroup(workerThreads, new MyThreadFactory("worker"));

		serviceHandler = new ServiceHandler(getExporters(), bizThreadPool);

		httpServerInitializer = new HttpServerInitializer(serviceHandler);

		bootstrap = new io.netty.bootstrap.ServerBootstrap();
		bootstrap.group(bossThreadPool, workerThreadPool)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, Constants.SO_BACKLOG)  //requested maximum length of the queue of incoming connections.
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
				.childOption(ChannelOption.SO_SNDBUF, Constants.SO_SNDBUF)
				.childOption(ChannelOption.SO_RCVBUF, Constants.SO_RCVBUF)
				.childOption(ChannelOption.TCP_NODELAY, true)  //禁用Nagle算法
				.childHandler(httpServerInitializer);
		try
		{
			channelFuture = bootstrap.bind( new InetSocketAddress(port)).sync();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.out.println("################# initNettyServer() ... finish! ##################");
	}


	private String serviceUri;

	public void setZkClient(ZkClient zkClient)
	{
		this.zkClient = zkClient;
	}

	public void setProxyFactory(ServiceProxyFactory proxyFactory)
	{
		this.proxyFactory = proxyFactory;
	}

	public void setServiceUri(String serviceUri)
	{
		this.serviceUri = serviceUri;
	}

	private String reportIp;

	private void reportInstance()
	{
		reportIp = System.getenv("JAVA_SERVER_REPORT_HOST");
		String instancePath = serviceUri + "/instance";
		/*创建实例路径*/
		zkClient.createPersistent(instancePath, true);
		/*准备服务上报数据*/
		JSONObject reportData = new JSONObject();
		reportData.put("report_ip",reportIp);
		reportData.put("port",port);
		/*上报服务*/
		String realPath = instancePath + "/server_1";
		try
		{
			if (zkClient.exists(realPath))
			{
				System.err.println("" + realPath);
				System.exit(-1);
			}
			zkClient.createEphemeral(realPath, reportData.toJSONString());
			System.out.println("############上报服务:" + reportData.toJSONString() + "########");

		}
		catch (Exception e)
		{
			System.err.println("上报服务失败：" + realPath + " " + reportData.toJSONString() );
			e.printStackTrace();
		}
	}



}
