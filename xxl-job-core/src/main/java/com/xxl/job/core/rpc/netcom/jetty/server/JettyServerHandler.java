package com.xxl.job.core.rpc.netcom.jetty.server;

import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.NetComServerFactory;
import com.xxl.job.core.rpc.serialize.HessianSerializer;
import com.xxl.job.core.util.HttpClientUtil;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * jetty handler
 * JettyServerHandler接收请求后的处理流程
 * @author xuxueli 2015-11-19 22:32:36
 */
public class JettyServerHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(JettyServerHandler.class);

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		// invoke 主要在这个方法
        RpcResponse rpcResponse = doInvoke(request);

        // serialize response
        byte[] responseBytes = HessianSerializer.serialize(rpcResponse);
		
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);

		// 响应结果
		OutputStream out = response.getOutputStream();
		out.write(responseBytes);
		out.flush();
		
	}

	// 通过上面的handle中的代码可以知道，主要的执行逻辑在doInvoke 的方法中
	private RpcResponse doInvoke(HttpServletRequest request) {
		try {
			// deserialize request
			// 读取请求数据
			byte[] requestBytes = HttpClientUtil.readBytes(request);
			if (requestBytes == null || requestBytes.length==0) {
				RpcResponse rpcResponse = new RpcResponse();
				rpcResponse.setError("RpcRequest byte[] is null");
				return rpcResponse;
			}
			// 通过hessian的序列化机制，反序列化得到字符串数据
			RpcRequest rpcRequest = (RpcRequest) HessianSerializer.deserialize(requestBytes, RpcRequest.class);

			// invoke
			// 得到参数之后，执行
			RpcResponse rpcResponse = NetComServerFactory.invokeService(rpcRequest, null);
			return rpcResponse;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			RpcResponse rpcResponse = new RpcResponse();
			rpcResponse.setError("Server-error:" + e.getMessage());
			return rpcResponse;
		}
	}

}
