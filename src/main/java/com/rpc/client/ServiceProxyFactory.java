package com.rpc.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rpc.annotation.API;

import com.rpc.server.codec.Message;
import com.rpc.util.DateUtil;
import com.rpc.util.MyThreadFactory;
import com.rpc.util.NettyUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiao on 2017/8/29.
 */
@Data
public class ServiceProxyFactory implements InvocationHandler
{
	private ZkClient zkClient;

	private int connectTimeout = 3000;

	private int ioThreads = 8;

	private EventLoopGroup ioThreadPool;

	private Map<Class<?>, Object> proxys = new ConcurrentHashMap<>();

	private volatile Map<String, MyCallback> callbacks = new ConcurrentHashMap<>();

	private HttpClientHandler clientHandler = new HttpClientHandler(this);

	private ChannelInitializer<Channel> clientInitializer = new HttpClientInitializer(clientHandler);

	private Bootstrap clientBootstrap = new Bootstrap();

	private List<String> uris = new ArrayList<>();

	private volatile Map<String, Service> uri2service = new ConcurrentHashMap<>();

	private volatile Map<String, Service> instancePath2service = new ConcurrentHashMap<>();

	private volatile Map<String, String> interface2uri = new ConcurrentHashMap<>();

	private volatile List<Class<?>> classes;

	private volatile Service brokerService;

	public ServiceProxyFactory(List<Service> services) throws ClassNotFoundException
	{
		if(services == null || services.size() == 0)
		{
			return ;
		}
		classes = new LinkedList<>();
		for(Service s : services)
		{
			Set<String> interfaces = s.getInterfaceNames();
			String serviceUri = s.getServiceUri();
			if (StringUtils.isEmpty(serviceUri))
				throw new IllegalArgumentException("service uri cannot be null or empty!");

			if (interfaces != null && interfaces.size() > 0)
			{
				for (String interfaceName : interfaces)
				{
					classes.add(Class.forName(interfaceName));
					interface2uri.put(interfaceName, serviceUri);
				}
			}
			uris.add(serviceUri);
			uri2service.put(serviceUri, s);
			instancePath2service.put(s.getInstancePath(), s);
		}
	}

	public void init()
	{
		initNettyClient();
		initProxy();
		initChannels();

	}

	private void initProxy()
	{
		for (Class<?> class1 : classes)
		{
			Object proxy = Proxy.newProxyInstance(ServiceProxyFactory.class.getClassLoader(), new Class[] { class1 }, this);
			this.proxys.put(class1, proxy);
		}
	}


	private void initNettyClient()
	{
		ioThreadPool = new NioEventLoopGroup(ioThreads, new MyThreadFactory("cio"));

		clientBootstrap.group(ioThreadPool)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
				.handler(clientInitializer);
	}

	private void initChannels()
	{
		for(Service s : uri2service.values())
		{
			String instancePath = s.getInstancePath();
			List<String> childs = null;
			try
			{
				childs = zkClient.getChildren(instancePath);
			}
			catch (ZkNoNodeException e)
			{
				System.out.println("获取服务实例失败");
			}
			//TODO 为了测试，默认取第一个服务实例
			String childPath = instancePath + "/" + childs.get(0);
			String txt = zkClient.readData(childPath, true);
			JSONObject json = JSON.parseObject(txt);
			Instance instance = JSON.toJavaObject(json, Instance.class);
			instance.setId(childs.get(0));
			connect(s, instance);
		}
	}

	private void connect(Service service, Instance instance)
	{
		Channel channel = null;
		try
		{
			System.out.println("远程服务建立连接：" + service.getServiceUri() + ":"  + instance.toString() );
			ChannelFuture cf = clientBootstrap.connect(instance.getReport_ip(), instance.getPort()).sync();
			channel = cf.channel();
		}
		catch (Exception e)
		{
			System.err.println("远程服务建立连接失败");
		}

		if(channel != null)
		{
			instance.setChannel(channel);
			instance.setConn_time(DateUtil.format(new Date()));
			service.setInstance(instance);
		}
	}

	void handleResponse(ChannelHandlerContext ctx, Message res)
	{
		String bsid = res.headers.getBsid();
		if (bsid == null)
		{
			System.out.println("收到无效的回复，请求ID等于空，bsid=null");
			System.out.println(res.toString());
			return;
		}

		MyCallback task = callbacks.get(bsid);

		if(task != null)
		{
			task.handle(res);
		}
		else
		{
			System.out.println("处理失败！");
		}
	}

	public <T> T getServiceProxy(Class<T> requiredType)
	{
		return (T) proxys.get(requiredType);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if ("toString".equals(methodName) && parameterTypes.length == 0)
		{
			return this.toString();
		}
		if ("hashCode".equals(methodName) && parameterTypes.length == 0)
		{
			return this.hashCode();
		}
		if ("equals".equals(methodName) && parameterTypes.length == 1)
		{
			return this.equals(args[0]);
		}

		JSONObject argsObj = new JSONObject();
		if( args != null && args.length > 0)
		{
			argsObj = (JSONObject) args[0];
		}

		API apiNameAnn = method.getAnnotation(API.class);

		String apiName = apiNameAnn == null ? methodName : apiNameAnn.value();

		String interfaceName = method.getDeclaringClass().getName();

		String uri = interface2uri.get(interfaceName);

		if(uri == null)
		{
			System.err.println("");
		}

		Object javaResult = null;

		JSONObject respBody = post(uri, apiName, argsObj);
		if (respBody != null && respBody.getIntValue("status") == 200)
		{
			javaResult = respBody.get("result");
		}
		else
		{
			throw new IllegalStateException("非200或无结果");
		}
		return javaResult;
	}

	public JSONObject post(String uri, String method, JSONObject args) //throws RPCException
	{
		Message req = new Message();

		Service service = brokerService != null ? brokerService : uri2service.get(uri);
		//TODO service == null
		req.headers.setMethod(method);
		req.headers.setService(uri);
		JSONObject body = new JSONObject();
		body.put("args",args);
		req.body = body;
		req.headers.bsid = NettyUtils.genBsid();

		Instance  instance = service.getInstances().get("server_1");
		//TODO instance ==null
		Channel channel = instance.getChannel();
		Message res = call(req, channel, null);

		return res != null ? res.body : null;
	}

	private Message call(Message req, Channel channel, Type returnType)
	{
		MyCallback callback = new MyCallback();

		String bsid = req.headers.getBsid();
		callbacks.put(bsid, callback);

		try
		{
			if (returnType != null && "void".equals(returnType.getTypeName()))
			{
				channel.writeAndFlush(req);
				return null;
			}
			channel.writeAndFlush(req);
			Message res = callback.get();

			return res;
		}
		finally
		{
			callbacks.remove(bsid);
		}
	}
}
