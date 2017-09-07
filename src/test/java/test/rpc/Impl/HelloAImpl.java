package test.rpc.Impl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import test.rpc.api.HelloA;

/**
 * Created by xiao on 2017/9/2.
 */
//@Service
public class HelloAImpl implements HelloA
{
	@Override
	public JSONObject hi(JSONObject jsonObject)
	{
		JSONObject response = new JSONObject();
		response.put("msg","Hello " + jsonObject.get("name"));
		return response;
	}
}
