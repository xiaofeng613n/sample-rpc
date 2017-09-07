package com.rpc.zk;

/**
 * Created by xiao on 2017/8/24.
 */
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

public class StringZkSerializer implements ZkSerializer
{

	@Override
	public byte[] serialize(Object data) throws ZkMarshallingError
	{
		if(data == null)
			return null;

		return ((String)data).getBytes();
	}

	@Override
	public Object deserialize(byte[] bytes) throws ZkMarshallingError
	{
		if(bytes == null)
			return  null;
		return new String(bytes);
	}

}