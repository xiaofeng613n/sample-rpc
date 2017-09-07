package test.rpc.Impl;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import test.rpc.api.HelloA;
import test.rpc.api.HelloB;

/**
 * Created by xiao on 2017/8/30.
 */
//@Service
@Data
public class HelloBImpl implements HelloB
{
	private HelloA helloA;

	@Override
	public JSONObject hello()
	{
		JSONObject json = new JSONObject();
		json.put("name","xiaofeng");
		return helloA.hi(json);
	}
}
