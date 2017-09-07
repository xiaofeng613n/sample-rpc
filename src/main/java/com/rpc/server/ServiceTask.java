package com.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.util.Date;
import com.alibaba.fastjson.JSONObject;
import com.rpc.server.codec.Message;

/**
 * Created by xiao on 2017/8/30.
 */
public class ServiceTask implements Runnable
{
	private ChannelHandlerContext ctx;

	private Message req;

	private Method method;

	private Object impl;

	private long recvTime;

	public ServiceTask(ChannelHandlerContext ctx, Message req, Method method, Object impl)
	{
		this.ctx = ctx;

		this.req = req;

		this.method = method;

		this.impl = impl;
	}

	public void setRecvTime(long recvTime)
	{
		this.recvTime = recvTime;
	}

	@Override
	public void run()
	{
		long startTime = System.currentTimeMillis();
		System.out.println("处理业务: " + req.headers.bsid + " "+ new Date(startTime));
		JSONObject args = req.body.getJSONObject("args");
		try
		{
			Object result;
			if( args == null)
			{
				result = method.invoke(impl);
			}
			else
			{
				result= method.invoke(impl, args);
			}
			req.setSuccessResponse(result);
		}
		catch (Exception e)
		{
			System.err.println("业务处理异常");
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();

		long cost = endTime - startTime;
		System.out.println(req.headers.method + "业务处理耗时：" + cost );
		System.out.println(req.headers.method + "实际耗时：" +  (endTime - recvTime)  );
		req.headers.type = 1;
		ctx.writeAndFlush(req);
	}

}
