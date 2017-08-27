package com.rpc.server;

import lombok.Data;

/**
 * Created by xiao on 2017/8/27.
 */
@Data
public class Exporter
{
	private Class<?> clazz;
	private Object Interface;

	public Exporter(Class<?> clazz,Object Interface)
	{
		this.clazz = clazz;
		this.Interface = Interface;
	}
}
