package com.rpc.client;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * Created by xiao on 2017/8/24.
 */

@Data
public class Instance
{
	private String id;

	private Channel channel;

	private String report_ip;

	private int port;

	private String conn_time;

	@Override
	public String toString()
	{
		return "Instance{ id="+id+",channel=" + channel+ ",report_ip=" + report_ip +", port=" + port + ", conn_time=" + conn_time  + '}';
	}
}