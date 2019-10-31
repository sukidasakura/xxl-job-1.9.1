package com.xxl.job.core.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * httpclient util
 * @author xuxueli 2015-10-31 19:50:41
 */
public class HttpClientUtil {
	private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	private static final int connectTimeout = 5000;

	private RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(getConnecttimeout())
			.setConnectTimeout(getConnecttimeout()).setConnectionRequestTimeout(getConnecttimeout())
			.build();

	private static HttpClientUtil instance = null;

	private HttpClientUtil() {
	}

	public static HttpClientUtil getInstance() {
		if (instance == null) {
			instance = new HttpClientUtil();
		}
		return instance;
	}

	/**
	 * post request
	 */
	public static byte[] postRequest(String reqURL, byte[] date) throws Exception {
		byte[] responseBytes = null;
		
		HttpPost httpPost = new HttpPost(reqURL);
		//CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpClient httpClient = HttpClients.custom().disableAutomaticRetries().build();	// disable retry

		try {
			// init post
			/*if (params != null && !params.isEmpty()) {
				List<NameValuePair> formParams = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
			}*/

			// timeout
			RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(10000)
                    .setSocketTimeout(10000)
                    .setConnectTimeout(10000)
                    .build();

			httpPost.setConfig(requestConfig);

			// data
			if (date != null) {
				httpPost.setEntity(new ByteArrayEntity(date, ContentType.DEFAULT_BINARY));
			}
			// do post
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (null != entity) {
				responseBytes = EntityUtils.toByteArray(entity);
				EntityUtils.consume(entity);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			httpPost.releaseConnection();
			try {
				httpClient.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return responseBytes;
	}
	
	/**
	 * read bytes from http request
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	public static final byte[] readBytes(HttpServletRequest request) throws IOException {
		request.setCharacterEncoding("UTF-8");
        int contentLen = request.getContentLength();
		InputStream is = request.getInputStream();
		if (contentLen > 0) {
			int readLen = 0;
			int readLengthThisTime = 0;
			byte[] message = new byte[contentLen];
			try {
				while (readLen != contentLen) {
					readLengthThisTime = is.read(message, readLen, contentLen - readLen);
					if (readLengthThisTime == -1) {
						break;
					}
					readLen += readLengthThisTime;
				}
				return message;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw e;
			}
		}
		return new byte[] {};
	}


	/**
	 * 发送 post请求
	 *
	 * @param httpUrl 地址
	 * @param params  参数(格式:key1=value1&key2=value2)
	 */
	public String sendHttpPost(String httpUrl, String params) {
		HttpPost httpPost = new HttpPost(httpUrl);
		try {
			StringEntity stringEntity = new StringEntity(params, "UTF-8");
			stringEntity.setContentType("application/x-www-form-urlencoded");
			httpPost.setEntity(stringEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sendHttpPost(httpPost);
	}


	/**
	 * 发送Post请求
	 *
	 * @param httpPost
	 * @return
	 */
	private String sendHttpPost(HttpPost httpPost) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		String responseContent = null;
		try {
			httpClient = HttpClients.createDefault();
			httpPost.setConfig(requestConfig);
			response = httpClient.execute(httpPost);
			entity = response.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return responseContent;
	}

	/**
	 * 发送 get请求
	 *
	 * @param httpUrl
	 */
	public String sendHttpGet(int connectTimeout, String httpUrl) {
		HttpGet httpGet = new HttpGet(httpUrl);
		return sendHttpGet(connectTimeout, httpGet);
	}


	/**
	 * 发送Get请求
	 *
	 * @param connectTimeout
	 * @param httpGet
	 * @return
	 */
	private String sendHttpGet(int connectTimeout, HttpGet httpGet) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		String responseContent = null;
		try {
			httpClient = HttpClients.createDefault();
			httpGet.setConfig(requestConfig(connectTimeout));
			response = httpClient.execute(httpGet);
			entity = response.getEntity();

			responseContent = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return responseContent;
	}


	private RequestConfig requestConfig(int connectTimeout) {
		if (connectTimeout == 0) {
			connectTimeout = HttpClientUtil.getConnecttimeout();
		}
		return RequestConfig.custom().setSocketTimeout(connectTimeout).setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout).build();
	}

	/**
	 * @return connecttimeout
	 */
	public static int getConnecttimeout() {
		return connectTimeout;
	}

}
