package com.xxl.job.admin.core.jobbean;

import com.xxl.job.admin.core.trigger.XxlJobTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * http job bean
 * “@DisallowConcurrentExecution” diable concurrent, thread size can not be only one, better given more
 *
 * xxl-job所有的任务触发最终都是通过这个类来执行，该类继承关系如下：
 * RemoteHttpJobBean > QuartzJobBean > Job
 * 当quartz监听到有任务需要触发时，会调用JobRunShell的run方法，在该类的run方法中，会调用当前任务的JOB_CLASS的execute方法，
 * 调用链最终会调用到RemoteHttpJobBean的executeInternal()
 *
 * @author xuxueli 2015-12-17 18:20:34
 */
//@DisallowConcurrentExecution
public class RemoteHttpJobBean extends QuartzJobBean {
	private static Logger logger = LoggerFactory.getLogger(RemoteHttpJobBean.class);

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {

		// load jobId
		JobKey jobKey = context.getTrigger().getJobKey();
		Integer jobId = Integer.valueOf(jobKey.getName());

		// trigger
		XxlJobTrigger.trigger(jobId);
	}

}