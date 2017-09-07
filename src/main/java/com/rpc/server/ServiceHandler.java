package com.rpc.server;

import com.rpc.annotation.API;
import com.rpc.server.codec.Headers;
import com.rpc.server.codec.Message;
import com.rpc.util.NettyUtils;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang3.StringUtils;


@Sharable
public class ServiceHandler extends DefaultChannelHandler
{
	private final Map<String, Object> iHandler = new HashMap<>();

	private final Map<String, Method> iMethod = new HashMap<>();

	private ExecutorService bizThreadPool;


	public ServiceHandler(List<Exporter> exporters, ExecutorService bizThreadPool)
	{
		this.bizThreadPool = bizThreadPool;

		for (Exporter exporter : exporters)
		{
			Class<?> interfaceClass = exporter.getClazz();
			if (interfaceClass == null)
			{
				throw new IllegalStateException("开放接口为空");
			}
			if (!interfaceClass.isInterface())
			{
				throw new IllegalStateException(interfaceClass + " 不是接口");
			}

			Object impl = exporter.getInterfaceImpl();
			if (impl == null)
			{
				throw new IllegalStateException("开放接口的实现对象为空");
			}

			if (!interfaceClass.isInstance(impl))
			{
				throw new IllegalStateException( impl +"没有实现接口"+ interfaceClass + " !");
			}

			Method[] methods = interfaceClass.getDeclaredMethods();

			for (Method method : methods)
			{
				API api = method.getAnnotation(API.class);

				String apiName = api == null ? method.getName() : api.value();

				if (iHandler.containsKey(apiName))
				{
					throw new IllegalArgumentException("ApiName:" + apiName + " 已被占用");
				}

				iHandler.put(apiName, exporter.getInterfaceImpl());
				iMethod.put(apiName, method);
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
	{
		long recvTime = System.currentTimeMillis();

		Message req = (Message) msg;

		Headers head = req.headers;

		String methodName = head.method;

		if (StringUtils.isEmpty(head.bsid))
		{
			head.bsid = NettyUtils.genBsid();
		}

		if("echo".equals(methodName))
		{
			bizThreadPool.execute(() -> {
				req.setSuccessResponse(req.body.get("args"));
				ctx.writeAndFlush(req);
			});
			return ;
		}
		else
		{
			System.out.println("serviceHandler接受请求：" + msg.toString());
		}

		Method method = iMethod.get(methodName);
		Object impl = iHandler.get(methodName);

		if(method == null || impl == null)
		{
			req.setErrorResponse("method not found:method=" + methodName);
			ctx.writeAndFlush(req);
			return ;
		}
		ServiceTask task = new ServiceTask(ctx, req, method, impl);
		task.setRecvTime(recvTime);

		bizThreadPool.execute(task);

	}
}
