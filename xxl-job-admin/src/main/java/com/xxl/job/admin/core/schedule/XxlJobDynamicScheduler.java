package com.xxl.job.admin.core.schedule;

import com.xxl.job.admin.core.jobbean.RemoteHttpJobBean;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobResource;
import com.xxl.job.admin.core.thread.JobFailMonitorHelper;
import com.xxl.job.admin.core.thread.JobRegistryMonitorHelper;
import com.xxl.job.admin.dao.*;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.rpc.netcom.NetComClientProxy;
import com.xxl.job.core.rpc.netcom.NetComServerFactory;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * base quartz scheduler util 调度中心启动时启动
 * 启动时主要是启动两个线程：
 * 1.用来监控自动注册上来的机器，达到自动注册的目的
 * 2.监控任务的执行状态，如若失败，则发送邮件预警
 * @author xuxueli 2015-12-19 16:13:53
 */
public final class XxlJobDynamicScheduler implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobDynamicScheduler.class);

    // ---------------------- param ----------------------

    // scheduler
    private static Scheduler scheduler;
    public void setScheduler(Scheduler scheduler) {
		XxlJobDynamicScheduler.scheduler = scheduler;
	}

    // 为提升系统安全性，调度中心和执行器进行安全性校验，双方AccessToken匹配才允许通讯；
    // 调度中心和执行器，可通过配置项 "xxl.job.accessToken" 进行AccessToken的设置。
    // 调度中心和执行器，如果需要正常通讯，只有两种设置；
    // - 设置一：调度中心和执行器，均不设置AccessToken；关闭安全性校验；
    // - 设置二：调度中心和执行器，设置了相同的AccessToken；
	// accessToken
    private static String accessToken;
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    // dao
    public static XxlJobLogDao xxlJobLogDao;
    public static XxlJobInfoDao xxlJobInfoDao;
    public static XxlJobRegistryDao xxlJobRegistryDao;
    public static XxlJobGroupDao xxlJobGroupDao;
    public static XxlJobResourceDao xxlJobResourceDao;
    public static AdminBiz adminBiz;

    // ---------------------- applicationContext ----------------------
    @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		XxlJobDynamicScheduler.xxlJobLogDao = applicationContext.getBean(XxlJobLogDao.class);
		XxlJobDynamicScheduler.xxlJobInfoDao = applicationContext.getBean(XxlJobInfoDao.class);
        XxlJobDynamicScheduler.xxlJobRegistryDao = applicationContext.getBean(XxlJobRegistryDao.class);
        XxlJobDynamicScheduler.xxlJobGroupDao = applicationContext.getBean(XxlJobGroupDao.class);
        XxlJobDynamicScheduler.xxlJobResourceDao = applicationContext.getBean(XxlJobResourceDao.class);
        XxlJobDynamicScheduler.adminBiz = applicationContext.getBean(AdminBiz.class);
	}

    // ---------------------- init + destroy ----------------------
    public void init() throws Exception {
        // admin registry monitor run 启动自动注册线程，获取类型为自动注册的执行器信息，完成机器的自动注册与发现
        JobRegistryMonitorHelper.getInstance().start();

        // admin monitor run 启动失败日志监控线程
        JobFailMonitorHelper.getInstance().start();

        // admin-server(spring-mvc)
        NetComServerFactory.putService(AdminBiz.class, XxlJobDynamicScheduler.adminBiz);
        NetComServerFactory.setAccessToken(accessToken);

        // valid
        Assert.notNull(scheduler, "quartz scheduler is null");
        logger.info(">>>>>>>>> init xxl-job admin success.");
    }

    public void destroy(){
        // admin registry stop
        JobRegistryMonitorHelper.getInstance().toStop();

        // admin monitor stop
        JobFailMonitorHelper.getInstance().toStop();
    }

    // ---------------------- executor-client ----------------------
    private static ConcurrentHashMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<String, ExecutorBiz>();
    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        // valid
        if (address==null || address.trim().length()==0) {
            return null;
        }

        // load-cache
        address = address.trim(); //删除前后两端空白字符
        // 查看缓存里面是否存在，如果存在则不需要去创建executorBiz了
        ExecutorBiz executorBiz = executorBizRepository.get(address);
        if (executorBiz != null) {
            return executorBiz;
        }

        // set-cache 创建ExecutorBiz的代理对象，重点在这个里面
        // NetComClientProxy这是一个factoryBean，所以我们主要看他的getObject方法就知道怎么创建对象并返回的
        executorBiz = (ExecutorBiz) new NetComClientProxy(ExecutorBiz.class, address, accessToken).getObject();
        executorBizRepository.put(address, executorBiz);
        return executorBiz;
    }

    // ---------------------- schedule util ----------------------

    /**
     * fill job info 把任务加入到指定执行器的调度器中
     *
     * @param jobInfo
     */
	public static void fillJobInfo(XxlJobInfo jobInfo) {
		// TriggerKey : name + group
        String group = String.valueOf(jobInfo.getJobGroup());
        String name = String.valueOf(jobInfo.getId());
        TriggerKey triggerKey = TriggerKey.triggerKey(name, group);

        try {
			Trigger trigger = scheduler.getTrigger(triggerKey);

			TriggerState triggerState = scheduler.getTriggerState(triggerKey);
			
			// parse params
			if (trigger!=null && trigger instanceof CronTriggerImpl) {
				String cronExpression = ((CronTriggerImpl) trigger).getCronExpression();
				jobInfo.setJobCron(cronExpression);
			}

			//JobKey jobKey = new JobKey(jobInfo.getJobName(), String.valueOf(jobInfo.getJobGroup()));
            //JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            //String jobClass = jobDetail.getJobClass().getName();

			if (triggerState!=null) {
				jobInfo.setJobStatus(triggerState.name());
			}
			
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
    /**
     * check if exists
     *
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
	public static boolean checkExists(String jobName, String jobGroup) throws SchedulerException{
		TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
		return scheduler.checkExists(triggerKey);
	}

    /**
     * addJob
     *
     * @param jobName
     * @param jobGroup
     * @param cronExpression
     * @return
     * @throws SchedulerException
     */
	public static boolean addJob(String jobName, String jobGroup, String cronExpression) throws SchedulerException {
    	// TriggerKey : name + group
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        JobKey jobKey = new JobKey(jobName, jobGroup);
        
        // TriggerKey valid if_exists
        if (checkExists(jobName, jobGroup)) {
            logger.info(">>>>>>>>> addJob fail, job already exist, jobGroup:{}, jobName:{}", jobGroup, jobName);
            return false;
        }
        
        // CronTrigger : TriggerKey + cronExpression
        // withMisfireHandlingInstructionDoNothing 忽略掉调度终止过程中忽略的调度
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
        // 定义一个trigger
        CronTrigger cronTrigger = TriggerBuilder
                .newTrigger()
                .withIdentity(triggerKey) // 定义name/group
                .withSchedule(cronScheduleBuilder) // 选用CronScheduleBuilder
                .build();

        // JobDetail : jobClass
		Class<? extends Job> jobClass_ = RemoteHttpJobBean.class;   // Class.forName(jobInfo.getJobClass());
        //定义一个JobDetail
		JobDetail jobDetail = JobBuilder.
                newJob(jobClass_)
                .withIdentity(jobKey)
                .build();
        /*if (jobInfo.getJobData()!=null) {
        	JobDataMap jobDataMap = jobDetail.getJobDataMap();
        	jobDataMap.putAll(JacksonUtil.readValue(jobInfo.getJobData(), Map.class));	
        	// JobExecutionContext context.getMergedJobDataMap().get("mailGuid");
		}*/
        
        // schedule : jobDetail + cronTrigger  加入这个调度
        Date date = scheduler.scheduleJob(jobDetail, cronTrigger);

        logger.info(">>>>>>>>>>> addJob success, jobDetail:{}, cronTrigger:{}, date:{}", jobDetail, cronTrigger, date);
        return true;
    }
    
    /**
     * rescheduleJob 重新调度任务
     *
     * @param jobGroup
     * @param jobName
     * @param cronExpression
     * @return
     * @throws SchedulerException
     */
	public static boolean rescheduleJob(String jobGroup, String jobName, String cronExpression) throws SchedulerException {
    	
    	// TriggerKey valid if_exists
        if (!checkExists(jobName, jobGroup)) {
        	logger.info(">>>>>>>>>>> rescheduleJob fail, job not exists, JobGroup:{}, JobName:{}", jobGroup, jobName);
            return false;
        }
        
        // TriggerKey : name + group
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        CronTrigger oldTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        if (oldTrigger != null) {
            // avoid repeat
            String oldCron = oldTrigger.getCronExpression();
            if (oldCron.equals(cronExpression)){
                return true;
            }

            // CronTrigger : TriggerKey + cronExpression
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
                    .cronSchedule(cronExpression) // Trigger已存在，那么更新相应的定时设置
                    .withMisfireHandlingInstructionDoNothing();
            oldTrigger = oldTrigger // 按照新的cronExpression表达式重新构建trigger
                    .getTriggerBuilder()
                    .withIdentity(triggerKey)
                    .withSchedule(cronScheduleBuilder)
                    .build();

            // rescheduleJob
            scheduler.rescheduleJob(triggerKey, oldTrigger);
        } else {
            // CronTrigger : TriggerKey + cronExpression
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder).build();

            // JobDetail-JobDataMap fresh
            JobKey jobKey = new JobKey(jobName, jobGroup);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            /*JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.clear();
            jobDataMap.putAll(JacksonUtil.readValue(jobInfo.getJobData(), Map.class));*/

            // Trigger fresh
            HashSet<Trigger> triggerSet = new HashSet<Trigger>();
            triggerSet.add(cronTrigger);

            scheduler.scheduleJob(jobDetail, triggerSet, true);
        }

        logger.info(">>>>>>>>>>> resumeJob success, JobGroup:{}, JobName:{}", jobGroup, jobName);
        return true;
    }
    
    /**
     * unscheduleJob
     *
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public static boolean removeJob(String jobName, String jobGroup) throws SchedulerException {
    	// TriggerKey : name + group
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        boolean result = false;
        if (checkExists(jobName, jobGroup)) {
            result = scheduler.unscheduleJob(triggerKey);
            logger.info(">>>>>>>>>>> removeJob, triggerKey:{}, result [{}]", triggerKey, result);
        }
        return true;
    }

    /**
     * pause 暂停任务
     *
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public static boolean pauseJob(String jobName, String jobGroup) throws SchedulerException {
    	// TriggerKey : name + group
    	TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        
        boolean result = false;
        if (checkExists(jobName, jobGroup)) {
            // 暂停触发器
            scheduler.pauseTrigger(triggerKey);
            result = true;
            logger.info(">>>>>>>>>>> pauseJob success, triggerKey:{}", triggerKey);
        } else {
        	logger.info(">>>>>>>>>>> pauseJob fail, triggerKey:{}", triggerKey);
        }
        return result;
    }
    
    /**
     * resume 恢复任务
     *
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public static boolean resumeJob(String jobName, String jobGroup) throws SchedulerException {
    	// TriggerKey : name + group
    	TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        
        boolean result = false;
        if (checkExists(jobName, jobGroup)) {
            // 恢复任务
            scheduler.resumeTrigger(triggerKey);
            result = true;
            logger.info(">>>>>>>>>>> resumeJob success, triggerKey:{}", triggerKey);
        } else {
        	logger.info(">>>>>>>>>>> resumeJob fail, triggerKey:{}", triggerKey);
        }
        return result;
    }
    
    /**
     * run 触发任务
     *
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public static boolean triggerJob(String jobName, String jobGroup) throws SchedulerException {
    	// TriggerKey : name + group
    	JobKey jobKey = new JobKey(jobName, jobGroup);
        
        boolean result = false;
        if (checkExists(jobName, jobGroup)) {
            // 调用quartz的Scheduler来触发任务
            scheduler.triggerJob(jobKey);
            result = true;
            logger.info(">>>>>>>>>>> runJob success, jobKey:{}", jobKey);
        } else {
        	logger.info(">>>>>>>>>>> runJob fail, jobKey:{}", jobKey);
        }
        return result;
    }

    /**
     * finaAllJobList
     *
     * @return
     *//*
    @Deprecated
    public static List<Map<String, Object>> finaAllJobList(){
        List<Map<String, Object>> jobList = new ArrayList<Map<String,Object>>();

        try {
            if (scheduler.getJobGroupNames()==null || scheduler.getJobGroupNames().size()==0) {
                return null;
            }
            String groupName = scheduler.getJobGroupNames().get(0);
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            if (jobKeys!=null && jobKeys.size()>0) {
                for (JobKey jobKey : jobKeys) {
                    TriggerKey triggerKey = TriggerKey.triggerKey(jobKey.getName(), Scheduler.DEFAULT_GROUP);
                    Trigger trigger = scheduler.getTrigger(triggerKey);
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    TriggerState triggerState = scheduler.getTriggerState(triggerKey);
                    Map<String, Object> jobMap = new HashMap<String, Object>();
                    jobMap.put("TriggerKey", triggerKey);
                    jobMap.put("Trigger", trigger);
                    jobMap.put("JobDetail", jobDetail);
                    jobMap.put("TriggerState", triggerState);
                    jobList.add(jobMap);
                }
            }

        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        return jobList;
    }*/

}