package test.rpc.api;

import com.alibaba.fastjson.JSONObject;
import com.rpc.annotation.API;

/**
 * Created by xiao on 2017/8/29.
 */
public interface HelloB
{
	@API(value = "hello")
	JSONObject hello();
}
