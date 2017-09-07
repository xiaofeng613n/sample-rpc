package com.rpc.zk;

import com.rpc.common.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by xiao on 2017/8/27.
 */
public class CustomZkClient extends ZkClient
{
	private static String zkHost;

	static {
		zkHost = System.getenv("JAVA_ZOOKEEPER_HOST");

		if(StringUtils.isEmpty(zkHost))
		{
			System.err.println("\nplease set environment variable: JAVA_ZOOKEEPER_HOST");
			System.exit(-1);
		}
	}

	public CustomZkClient(int sessionTimeout, int connectionTimeout)
	{
		super(zkHost, sessionTimeout, connectionTimeout, new StringZkSerializer());
	}

	public CustomZkClient()
	{
		super(zkHost, Constants.ZK_SESSION_TIMEOUT, Constants.ZK_CONNECT_TIMEOUT, new StringZkSerializer());
	}
}
