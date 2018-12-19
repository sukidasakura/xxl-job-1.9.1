package com.xxl.job.core.rpc.netcom;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.jetty.server.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * netcom init
 * @author xuxueli 2015-10-31 22:54:27
 */
public class NetComServerFactory  {
	private static final Logger logger = LoggerFactory.getLogger(NetComServerFactory.class);

	// ---------------------- server start ----------------------
	JettyServer server = new JettyServer();
	public void start(int port, String ip, String appName) throws Exception {
		server.start(port, ip, appName);
	}

	// ---------------------- server destroy ----------------------
	public void destroy(){
		server.destroy();
	}

	// ---------------------- server instance ----------------------
	/**
	 * init local rpc service map
	 */
	private static Map<String, Object> serviceMap = new HashMap<String, Object>();
	private static String accessToken;
	public static void putService(Class<?> iface, Object serviceBean){
		serviceMap.put(iface.getName(), serviceBean);
	}
	public static void setAccessToken(String accessToken) {
		NetComServerFactory.accessToken = accessToken;
	}
	public static RpcResponse invokeService(RpcRequest request, Object serviceBean) {
		// request中的数据结构，可以看NetComClientProxy中的getObject 方法
		if (serviceBean==null) {
			// 这个serviceBean就是在执行器启动的时候，initExecutorServer()这个方法中，
			// 将一个ExecutorBiz的实例放进去了，此处通过classname来获取这个实例
			serviceBean = serviceMap.get(request.getClassName());
		}
		if (serviceBean == null) {
			// TODO
		}

		RpcResponse response = new RpcResponse();

		//判断是否超时
		if (System.currentTimeMillis() - request.getCreateMillisTime() > 180000) {
			response.setResult(new ReturnT<String>(ReturnT.FAIL_CODE, "The timestamp difference between admin and executor exceeds the limit."));
			return response;
		}
		// 数据校验，验证token是否匹配，前提是token不为空
		if (accessToken!=null && accessToken.trim().length()>0 && !accessToken.trim().equals(request.getAccessToken())) {
			response.setResult(new ReturnT<String>(ReturnT.FAIL_CODE, "The access token[" + request.getAccessToken() + "] is wrong."));
			return response;
		}

		try {
			// 获取class
			Class<?> serviceClass = serviceBean.getClass();
			// 拿到请求中的方法名字，此处这个值是 run 方法
			String methodName = request.getMethodName();
			// 方法类型
			Class<?>[] parameterTypes = request.getParameterTypes();
			// 方法参数
			Object[] parameters = request.getParameters();

			// spring的工具类，创建一个fastClass实例
			FastClass serviceFastClass = FastClass.create(serviceClass);
			FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

			logger.info("serviceClass:" + serviceClass);
			logger.info("methodName:" + methodName);
			logger.info("parameters:" + parameters[0]);

			// 拿到方法之后执行方法的invoke
			// 通过调度中心发过来的参数，以及执行器的处理逻辑，我们有理由可以得出此时是执行的是ExecutorBizImpl中的run方法
			Object result = serviceFastMethod.invoke(serviceBean, parameters);

			response.setResult(result);
		} catch (Throwable t) {
			t.printStackTrace();
			response.setError(t.getMessage());
		}

		return response;
	}

}
