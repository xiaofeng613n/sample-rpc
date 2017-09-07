package com.rpc.client;

import com.rpc.common.Constants;
import com.rpc.server.codec.JsonCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by xiao on 2017/8/24.
 */

public class HttpClientInitializer extends ChannelInitializer<Channel>
{

	private ChannelHandlerAdapter serviceHandler;

	public HttpClientInitializer(ChannelHandlerAdapter bizHandler)
	{
		this.serviceHandler = bizHandler;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception
	{

		ChannelPipeline p = ch.pipeline();

		//p.addLast(new LoggingHandler(LogLevel.TRACE));

		p.addLast("codec", new HttpClientCodec());

		p.addLast("decompressor", new HttpContentDecompressor());

		p.addLast("aggegator", new HttpObjectAggregator(Constants.MAX_DATA_LENGTH));

		p.addLast("jsoncodec", new JsonCodec() );

		p.addLast("handler", serviceHandler);
	}

}