package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.core.schedule.XxlJobDynamicScheduler;
import com.xxl.job.core.enums.RegistryConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * job registry instance 启动自动注册线程，获取类型为自动注册的执行器信息，完成机器的自动注册与发现
 * @author xuxueli 2016-10-02 19:10:24
 */
public class JobRegistryMonitorHelper {
	private static Logger logger = LoggerFactory.getLogger(JobRegistryMonitorHelper.class);

	private static JobRegistryMonitorHelper instance = new JobRegistryMonitorHelper();
	public static JobRegistryMonitorHelper getInstance(){
		return instance;
	}

	private Thread registryThread;
	private volatile boolean toStop = false;
	public void start(){
		// 创建一个线程
		registryThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// 当toStop为false时进入该循环
				while (!toStop) {
					try {
						// auto registry group 获取类型为自动注册的执行器地址列表。执行器地址类型：0=自动注册、1=手动录入
						List<XxlJobGroup> groupList = XxlJobDynamicScheduler.xxlJobGroupDao.findByAddressType(0);
						if (CollectionUtils.isNotEmpty(groupList)) {

							// remove dead address (admin/executor)
							// 删除90秒之内没有更新信息的注册机器，90秒没有心跳信息返回，代表及其已经出现问题，故移除
							XxlJobDynamicScheduler.xxlJobRegistryDao.removeDead(RegistryConfig.DEAD_TIMEOUT);

							// fresh online address (admin/executor) 查询在90秒之内有过更新的机器列表
							HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
							List<XxlJobRegistry> list = XxlJobDynamicScheduler.xxlJobRegistryDao.findAll(RegistryConfig.DEAD_TIMEOUT);
							if (list != null) {
								// 循环注册机器列表，根据执行器不同，将这些机器列表区分拿出来
								for (XxlJobRegistry item: list) {
									// 判断该机器注册信息RegistryGroup, RegistType是否是Executor，Executor代表该机器是注册到执行器上面的
									// RegistType分为两种，ADMIN和EXECUTOR
									if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
										// 获取注册的执行器KEY（也就是执行器组别）
										String appName = item.getRegistryKey();
										List<String> registryList = appAddressMap.get(appName);
										if (registryList == null) {
											registryList = new ArrayList<String>();
										}

										if (!registryList.contains(item.getRegistryValue())) {
											registryList.add(item.getRegistryValue());
										}
										// 收集机器信息，根据执行器做区分
										appAddressMap.put(appName, registryList);
									}
								}
							}

							// fresh group address 遍历执行器列表
							for (XxlJobGroup group: groupList) {
								// 通过执行器的APP_NAME 拿出他下面的集群机器地址
								List<String> registryList = appAddressMap.get(group.getAppName());
								String addressListStr = null;
								if (CollectionUtils.isNotEmpty(registryList)) {
									Collections.sort(registryList);
									// 转为String, 通过逗号分隔
									addressListStr = StringUtils.join(registryList, ",");
								}
								group.setAddressList(addressListStr);
								// 将这个执行器的集群机器地址列表，写入到数据库
								XxlJobDynamicScheduler.xxlJobGroupDao.update(group);
							}
						}
					} catch (Exception e) {
						logger.error("job registry instance error:{}", e);
					}
					try {
						TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
					} catch (InterruptedException e) {
						logger.error("job registry instance error:{}", e);
					}
				}
			}
		});
		registryThread.setDaemon(true);
		// 启动线程
		registryThread.start();
	}

	public void toStop(){
		toStop = true;
		// interrupt and wait
		registryThread.interrupt();
		try {
			registryThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
