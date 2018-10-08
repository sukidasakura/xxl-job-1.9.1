package com.xxl.job.core.rpc.netcom;

import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.jetty.client.JettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * rpc proxy
 * 这个代理对象的invoke里面并没有执行目标类的方法，而是将目标类的信息包装好，发送给执行器那一端来做。
 * @author xuxueli 2015-10-29 20:18:32
 */
public class NetComClientProxy implements FactoryBean<Object> {
	private static final Logger logger = LoggerFactory.getLogger(NetComClientProxy.class);

	// ---------------------- config ----------------------
	private Class<?> iface;
	private String serverAddress;
	private String accessToken;
	private JettyClient client = new JettyClient();
	public NetComClientProxy(Class<?> iface, String serverAddress, String accessToken) {
		this.iface = iface;
		this.serverAddress = serverAddress;
		this.accessToken = accessToken;
	}

	@Override
	public Object getObject() throws Exception {

		/* 动态代理。newProxyInstance有3个参数：
			1.ClassLoader loader 指明生成代理对象使用哪个类装载器
			2.Class<?>[] interfaces 指明生成哪个对象的代理对象，通过接口指定
			3.InvocationHandler h 指明这个代理对象要做什么事情。每个动态代理类都要实现这个接口，
			并且每个代理类的实例都关联到了一个handler，当我们通过代理对象调用一个方法的时候，
			这个方法的调用就会被转发为由InvocationHandler这个接口的invoke方法来进行调用。
		*/
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class[] { iface },
				new InvocationHandler() {
			// 这个代理对象的Invoke里面并没有执行目标类的方法，而是将目标类的信息包装好，发送给执行器那一端来做。
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// 三个参数分别是：proxy 指代我们所代理的那个真实对象
						// method 我们所要调用的那个真实对象
						// args 调用真实对象某个方法时接受的参数

						// filter method like "Object.toString()"
						if (Object.class.getName().equals(method.getDeclaringClass().getName())) {
							logger.error(">>>>>>>>>>> xxl-rpc proxy class-method not support [{}.{}]", method.getDeclaringClass().getName(), method.getName());
							throw new RuntimeException("xxl-rpc proxy class-method not support");
						}
						
						// request
						// 重点。创建request信息，发送HTTP请求到执行器服务器上。
						RpcRequest request = new RpcRequest();
	                    request.setServerAddress(serverAddress); // 服务器地址
	                    request.setCreateMillisTime(System.currentTimeMillis()); // 创建时间，用于判断请求是否超时
	                    request.setAccessToken(accessToken); // 数据校验
						// 将目标类的class名称传给执行器，让那边来创建对象，并执行逻辑代码
	                    request.setClassName(method.getDeclaringClass().getName());
	                    request.setMethodName(method.getName()); // 方法名称为run
	                    request.setParameterTypes(method.getParameterTypes()); // 参数类型
	                    request.setParameters(args); // 参数

	                    // send
	                    RpcResponse response = client.send(request); // 发送HTTP请求
	                    
	                    // valid response
						if (response == null) {
							logger.error(">>>>>>>>>>> xxl-rpc netty response not found.");
							throw new Exception(">>>>>>>>>>> xxl-rpc netty response not found.");
						}
	                    if (response.isError()) {
	                        throw new RuntimeException(response.getError());
	                    } else {
	                        return response.getResult();
	                    }
	                   
					}
				});
	}
	@Override
	public Class<?> getObjectType() {
		return iface;
	}
	@Override
	public boolean isSingleton() {
		return false;
	}

}
