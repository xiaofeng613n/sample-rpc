package com.rpc.client;

import com.rpc.common.Constants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by xiao on 2017/8/24.
 */

public class Service
{
	private String serviceUri;

	private String instancePath;

	private Set<String> interfaceNames;

	private volatile Map<String, Instance> instances = new ConcurrentHashMap<>();

	public Service()
	{

	}

	public Service(String uri)
	{
		this.serviceUri = uri;
	}

	public void setServiceUri(String uri)
	{
		this.serviceUri = uri;
	}

	public String getInstancePath()
	{
		return (instancePath == null) ? (this.serviceUri + Constants.INSTANCE_PATH_SUFFIX) : instancePath;
	}

	public Set<String> getInterfaceNames()
	{
		return interfaceNames;
	}

	public void setInterfaceNames(Set<String> interfaceNames)
	{
		this.interfaceNames = interfaceNames;
	}


	public void setInstance(Instance i)
	{
		instances.put(i.getId(), i);
	}

	public Map<String, Instance> getInstances()
	{
		return instances;
	}

	public String getServiceUri()
	{
		return serviceUri;
	}

}