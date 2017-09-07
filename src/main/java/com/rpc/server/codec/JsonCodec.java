package com.rpc.server.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

import com.rpc.util.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Created by xiao on 2017/8/24.
 */

public class JsonCodec extends MessageToMessageCodec<DefaultHttpMessage, Message>
{

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception
	{
		String txt = "Response body is null!";
		if (msg.body != null)
		{
			Object result = msg.body.get("result");
			txt = result == null ?
					JSON.toJSONString(msg.body, SerializerFeature.WriteMapNullValue)
					: JSON.toJSONString(msg.body);
		}

		byte[] arr = txt.getBytes();

		ByteBuf buff = Unpooled.wrappedBuffer(arr);

		DefaultHttpMessage req;

		HttpHeaders headers;
		if (msg.isCall())
		{
			req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/", buff);
			headers = req.headers();

			Headers.parse(msg.headers,headers);
		}
		else if (msg.isReply())
		{
			req = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buff);
			headers = req.headers();
			headers.add("s",msg.headers.service);
			headers.add("m",msg.headers.method);
			headers.add("b",msg.headers.bsid);
			//Headers.setResponseHeader();
		}
		else
		{
			//TODO 返回错误
			return;
		}
		headers.set(Names.CONTENT_TYPE, "application/json");
		headers.set(Names.CONTENT_LENGTH, arr.length);
		out.add(req);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, DefaultHttpMessage msg, List<Object> out) throws Exception
	{
		HttpHeaders headers = msg.headers();

		String json;

		byte[] orgarray = null;

		Message msgObj = new Message();

		if (msg instanceof DefaultFullHttpRequest)
		{
			DefaultFullHttpRequest req = (DefaultFullHttpRequest) msg;
			if (req.getMethod() == HttpMethod.POST)
			{
				ByteBuf bytebuf = req.content();
				orgarray = new byte[bytebuf.readableBytes()];
				bytebuf.readBytes(orgarray);
			}
		}
		else if (msg instanceof DefaultFullHttpResponse)
		{
			DefaultFullHttpResponse res = (DefaultFullHttpResponse) msg;
			ByteBuf bytebuf = res.content();
			orgarray = new byte[bytebuf.readableBytes()];
			bytebuf.readBytes(orgarray);
		}

		if (orgarray == null || orgarray.length == 0)
		{
			DefaultHttpMessage resp = NettyUtils.createErrorResponse(headers, "请求体为空");
			ctx.writeAndFlush(resp);
			return;
		}

		json = new String(orgarray);

		try
		{
			msgObj.body = JSON.parseObject(json);
		}
		catch(Exception e)
		{
			DefaultHttpMessage resp = NettyUtils.createErrorResponse(headers, "请求体中JSON格式不正确");
			ctx.writeAndFlush(resp);
			return ;
		}

		msgObj.headers.setService(headers.get("s"));
		msgObj.headers.setMethod(headers.get("m"));
		msgObj.headers.setBsid(headers.get("b"));

		out.add(msgObj);

	}

}