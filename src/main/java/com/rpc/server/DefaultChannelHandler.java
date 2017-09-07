package com.rpc.server;

import com.rpc.util.NettyUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Created by xiao on 2017/8/27.
 */
public class DefaultChannelHandler extends ChannelHandlerAdapter
{

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception
	{
		System.out.println("channelRegistered:" + ctx.channel());
		super.channelRegistered(ctx);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		NettyUtils.exitIfOutOfMemory(cause);
		Channel ch = ctx.channel();
		boolean health = NettyUtils.checkChannelHealth(ch);
		System.err.println("");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		System.out.println("channelInactive:" + ctx.channel());
		ctx.fireChannelInactive();
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception
	{
		System.out.println("close:" + ctx.channel()  + promise);
		ctx.close(promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception
	{
		System.out.println("disconnect:" + ctx.channel()  + promise);
		ctx.disconnect(promise);
	}
}