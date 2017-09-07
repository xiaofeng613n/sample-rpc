package com.rpc.server.codec;

import com.rpc.common.Constants;
import com.rpc.common.Keys;
/**
 * Created by xiao on 2017/8/24.
 */

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.Data;

@Data
public class Headers
{
    public String service;

    public String caller;

    public String method;

    public String bsid;

    public int return_void;

    public int type;


    public static HttpHeaders parse(Headers head)
    {
        return parse(head, new DefaultHttpHeaders());
    }

    public static HttpHeaders parse(Headers head, HttpHeaders headers)
    {

        if (head.service != null)
            headers.set(Keys.SERVICE, head.service);

        if (head.caller != null)
            headers.set(Keys.CALLER, head.caller);

        if (head.method != null)
            headers.set(Keys.METHOD, head.method);

        if (head.bsid != null)
            headers.set(Keys.BIZ_SESSION_ID, head.bsid);

        headers.set(Keys.TYPE, head.type);

        headers.set(Keys.RETURN_VOID, head.return_void);

        return headers;
    }

	public static void setResponseHeader(HttpHeaders headers, HttpHeaders refer)
	{
		if (refer != null)
		{
			String bsid = refer.get(Keys.BIZ_SESSION_ID);
			String service = refer.get(Keys.SERVICE);
			String method = refer.get(Keys.METHOD);
			String caller = refer.get(Keys.CALLER);

			String returnVoid = refer.get(Keys.RETURN_VOID);

			if (service != null)
				headers.set(Keys.SERVICE, service);

			if (caller != null)
				headers.set(Keys.CALLER, caller);

			if (method != null)
				headers.set(Keys.METHOD, method);

			if (bsid != null)
				headers.set(Keys.BIZ_SESSION_ID, bsid);

			if (returnVoid != null)
				headers.set(Keys.RETURN_VOID, returnVoid);

			headers.set(Keys.TYPE, Constants.MSG_TYPE_REPLY);
		}
	}
}
