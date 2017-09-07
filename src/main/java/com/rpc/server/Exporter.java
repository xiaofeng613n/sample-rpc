package com.rpc.server;

import lombok.Data;

/**
 * Created by xiao on 2017/8/27.
 */
@Data
public class Exporter
{
	private Class<?> clazz;

	private Object interfaceImpl;

	public Exporter(Class<?> clazz ,Object interfaceImpl)
	{
		this.clazz = clazz;
		this.interfaceImpl = interfaceImpl;
	}
}
