package com.rpc.server;

import com.rpc.common.Constants;
import com.rpc.server.codec.JsonCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
/**
 * Created by xiao on 2017/8/24.
 */

public class HttpServerInitializer extends ChannelInitializer<Channel>{

	private ServiceHandler serviceHandler;

	public HttpServerInitializer(ServiceHandler serviceHandler)
	{
		this.serviceHandler = serviceHandler;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {

		ChannelPipeline p = ch.pipeline();

	//	p.addLast(new LoggingHandler(LogLevel.TRACE));

		p.addLast("codec", new HttpServerCodec());

		p.addLast("aggregator", new HttpObjectAggregator(Constants.MAX_DATA_LENGTH));

		p.addLast("compressor", new HttpContentCompressor(1));

		p.addLast("jsoncodec", new JsonCodec());

		p.addLast("handler", serviceHandler);
	}

}