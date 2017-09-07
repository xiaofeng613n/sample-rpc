package com.rpc.server.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rpc.common.*;
/**
 * Created by xiao on 2017/8/24.
 */

public class Message
{
	public Headers headers = new Headers();

	public JSONObject body;

	@Override
	public String toString()
	{
		return JSON.toJSONString(this);
	}

	public boolean isCall()
	{
		return headers.type == Constants.MSG_TYPE_CALL;
	}

	public boolean isReply()
	{
		return headers.type == Constants.MSG_TYPE_REPLY;
	}


	public void setErrorResponse(String msg)
	{
		body.put(Keys.STATUS_CODE, -1);
		body.put("msg",msg);
		headers.type =  Constants.MSG_TYPE_REPLY;;
	}

	public void setSuccessResponse(Object result)
	{
		body = new JSONObject(true);

		body.put(Keys.STATUS_CODE, 200);

		body.put(Keys.RES, result);

		headers.type = Constants.MSG_TYPE_REPLY;;
	}

}