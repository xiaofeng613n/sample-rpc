package com.rpc.common;

/**
 * Created by xiao on 2017/8/24.
 */
public class Constants {

	public static final int MAX_DATA_LENGTH = 128 * 1024 * 1024;

	public static final int SO_BACKLOG = 65535;

	public static final int SO_SNDBUF = 32 * 1024;

	public static final int SO_RCVBUF = 32 * 1024;

	public static final int ZK_CONNECT_TIMEOUT = 30*1000;

	public static final int ZK_SESSION_TIMEOUT = 30*1000;

	public static final String INSTANCE_PATH_SUFFIX = "/instance";

	public static final int MSG_TYPE_CALL = 0;

	public static final int MSG_TYPE_REPLY = 1;

}