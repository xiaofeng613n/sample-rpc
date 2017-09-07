package com.rpc.client;


import com.rpc.server.codec.Message;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
/**
 * Created by xiao on 2017/8/24.
 */

@Sharable
public class HttpClientHandler extends ChannelHandlerAdapter
{
	public ServiceProxyFactory client;

	public HttpClientHandler(ServiceProxyFactory client)
	{
		this.client = client;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception
	{
		System.out.println("channelRegistered:"+ ctx.channel());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
	{
		Message res = (Message)msg;
		if(res != null)
		{
			System.out.println(res.toString());
			client.handleResponse(ctx, res);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		System.out.println("channelInactive" + ctx.channel());
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		System.err.println("exceptionCaught" + ctx.channel() + cause);
	}
}