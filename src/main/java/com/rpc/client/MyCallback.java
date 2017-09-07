package com.rpc.client;

import com.rpc.server.codec.Message;
import java.util.concurrent.CountDownLatch;

/**
 * Created by xiao on 2017/8/24.
 */

public class MyCallback
{
	private volatile Message response;

	public CountDownLatch count = new CountDownLatch(1);

	public Message get()
	{
		try
		{
			count.await();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return response;
	}

	public void handle( Message response )
	{
		count.countDown();
		this.response = response;
	}
}