package com.rpc.util;


import java.util.concurrent.ThreadFactory;

/**
 * Created by xiao on 2017/8/27.
 */
public class MyThreadFactory implements ThreadFactory
{
	private String threadName;

	public MyThreadFactory(String threadName)
	{
		this.threadName = threadName;
	}

	@Override
	public Thread newThread(Runnable r)
	{
		Thread thread = new Thread(r,threadName);
		return thread;
	}
}
