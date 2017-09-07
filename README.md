###### 说明：
该项目实现了基本的rpc服务调用功能，服务之间通过
netty建立tcp连接，调用远程方法的时候，用jdk动态代理封装了交互过程

###### 主要技术：  
netty 服务通信  
zookeeper 服务主从发现  
JDK动态代理+反射

###### 配置：
在环境变量中配置zk地址 JAVA_ZOOKEEPER_HOST

###### 时序图：
```sequence
browser->>projectB:http请求
projectB->>projectA:tcp请求(channel.writeAndFlush(req))
projectA->>projectB: 返回结果
projectB->>browser:返回结果
```

com.rpc.server.ServerBootstrap netty服务端  
ServiceProxyFactory netty客户端  
据配置的远程服务，从zk上获得服务信息ip+port，与远程服务建立连接channel，调用远程服务即向对应的channel发送消息

###### 测试说明：  
Test启动服务projectA
Test1启动服务projectB

projectB服务的测试接口中，通过rpc调用，请求了服务projectA的方法。

---

请求服务A

192.168.1.100:40000  
head：
s：/biz/projectA/app
m:hi  
body：
```
{
	"args":{
		"name":"xiaofddeng"
	}
}
```

返回结果：

```
{
    "status": 200,
    "result": {
        "msg": "Hello xiaofeng"
    }
}
```

---

请求服务B  
192.168.1.100:40001  
head：  
s：/biz/projectB/app
m:hello  
body：
```
{
}
```

返回结果：

```
{
    "status": 200,
    "result": {
        "msg": "Hello xiaofeng"
    }
}
```

###### TODO： 
1. 服务调用参数没有规范化，暂时只是用一个JSONObject
可以封装好JSONObject到javabean的转换，同时做参数校验
2. 服务路由
3. 判空，很多地方还没有做判断处理
4. 日志
