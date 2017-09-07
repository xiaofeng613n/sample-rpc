package com.rpc.util;


import com.alibaba.fastjson.JSONObject;
import com.rpc.common.Keys;
import com.rpc.server.codec.Headers;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.concurrent.atomic.AtomicLong;
/**
 * Created by xiao on 2017/8/24.
 */

public class NettyUtils {

	public static boolean EXIT_IF_OUT_OF_MEM = false;

	protected static final AtomicLong offset = new AtomicLong(0);

	protected static final int BITS = 20;

	protected static final long MAX_COUNT_PER_MILLIS = 1 << BITS;

	public static void exitIfOutOfMemory(Throwable e)
	{
		if(e instanceof OutOfMemoryError)
		{
			System.err.println("OutOfMemoryError!!!!!! EXIT_IF_OUT_OF_MEM: " + EXIT_IF_OUT_OF_MEM);

			if(EXIT_IF_OUT_OF_MEM)
			{
				System.err.println("强制退出!!!!");
				System.exit(-1);
			}
			else
			{
				System.err.println("忽略!!!!");
			}
		}
	}

	/**
	 * 通过Bsid中数字能够知道大致请求的时间(如果是用的rpcClient的话)
	 *
	 * <pre>
	 * 		目前是 currentTimeMillis * (2^20) + offset.incrementAndGet()
	 *
	 * 		通过 Bsid / (2^20 * 1000) 能够得到秒
	 *
	 * </pre>
	 *
	 */
	public static String genBsid()
	{
		long currentTime = System.currentTimeMillis();
		long count = offset.incrementAndGet();
		while(count >= MAX_COUNT_PER_MILLIS){
			synchronized (NettyUtils.class){
				if(offset.get() >= MAX_COUNT_PER_MILLIS){
					offset.set(0);
				}
			}
			count = offset.incrementAndGet();
		}
		return "s-" + ((currentTime << BITS) + count);
	}

	public static boolean checkChannelHealth(Channel channel)
	{
		if (channel == null)
			return false;

		boolean isOpen = channel.isOpen();

		boolean isActive = channel.isActive();

		boolean isRegistered = channel.isRegistered();

		return isOpen && isActive && isRegistered;
	}

	public static DefaultFullHttpResponse createErrorResponse(HttpHeaders refer,String msg)
	{
		JSONObject respJson = new JSONObject();
		respJson.put(Keys.ERROR, msg);
		respJson.put(Keys.STATUS_CODE, -1);

		String respStr = respJson.toJSONString();
		ByteBuf buff = Unpooled.wrappedBuffer(respStr.getBytes());
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buff);

		HttpHeaders respHeader = resp.headers();
		if (refer != null)
		{
			Headers.setResponseHeader(respHeader, refer);
		}
		respHeader.set(Names.CONTENT_LENGTH, buff.readableBytes());
		respHeader.set(Names.CONTENT_TYPE, "application/json");

		return resp;
	}

	/*暂时未使用*/
	public static DefaultFullHttpResponse createSuccResponse(HttpHeaders refer, Object result)
	{
		JSONObject respJson = new JSONObject();
		respJson.put(Keys.STATUS_CODE, 200);
		respJson.put(Keys.RES, result);

		String respStr = respJson.toJSONString();
		ByteBuf buff = Unpooled.wrappedBuffer(respStr.getBytes());

		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buff);
		HttpHeaders respHeader = resp.headers();
		if (refer != null)
		{
			Headers.setResponseHeader(respHeader, refer);
		}
		respHeader.set(HttpHeaders.Names.CONTENT_LENGTH, buff.readableBytes());
		respHeader.set(HttpHeaders.Names.CONTENT_TYPE, "application/json");

		return resp;
	}
}