package com.xxl.job.admin.core.model;

import com.xxl.job.core.entity.presto.PrestoParam;

import java.util.Date;
import java.util.Map;

/**
 * xxl-job info
 * @author xuxueli  2016-1-12 18:25:49
 */
public class XxlJobInfo {

	/** 主键ID  (JobKey.name) **/
	private int id;

	/** 执行器主键ID (JobKey.group)**/
	private int jobGroup;
	/** 所属文件夹ID **/
	private int folderId;
	/** 任务执行Cron表达式 **/
	private String jobCron;
	/** 任务名称 **/
	private String jobName;
	/** 任务描述 **/
	private String jobDesc;

	private String addTime;
	private String updateTime;

	private String author;		// 负责人
    private String alarmEmail;	// 报警邮件

	/** 执行器路由策略 **/
	private String executorRouteStrategy;
	/** 执行器，任务Handler名称 **/
	private String executorHandler;
	/** 执行器，任务参数 **/
	private String executorParam;

	/** 阻塞处理策略 **/
	private String executorBlockStrategy;
	/** 失败处理策略 **/
	private String executorFailStrategy;

	/** glue类型 **/
	private String glueType;
	/** glue源代码 **/
	private String glueSource;
	/** glue备注 **/
	private String glueRemark;
	/** glue更新时间 **/
	private String glueUpdateTime;

	private String childJobId;		// 子任务ID，多个逗号分隔

	/** 任务状态（base on quartz）从quartz拷贝过来的 **/
	private String jobStatus;

	/** 资源ID，多个逗号分隔 **/
	private String resourceId;

	/** 资源ID，多个逗号分隔 **/
	private String[] resourceIdArray;

	/** 自定义参数，可能多个，格式为{"param1":"1","param2":"2"}，需要转化为JSONObject **/
	private String customParam;

	private PrestoParam prestoParam;

	public String getCustomParam() {
		return customParam;
	}

	public void setCustomParam(String customParam) {
		this.customParam = customParam;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(int jobGroup) {
		this.jobGroup = jobGroup;
	}

	public String getJobCron() {
		return jobCron;
	}

	public void setJobCron(String jobCron) {
		this.jobCron = jobCron;
	}

	public String getJobDesc() {
		return jobDesc;
	}

	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAlarmEmail() {
		return alarmEmail;
	}

	public void setAlarmEmail(String alarmEmail) {
		this.alarmEmail = alarmEmail;
	}

    public String getExecutorRouteStrategy() {
        return executorRouteStrategy;
    }

    public void setExecutorRouteStrategy(String executorRouteStrategy) {
        this.executorRouteStrategy = executorRouteStrategy;
    }

    public String getExecutorHandler() {
		return executorHandler;
	}

	public void setExecutorHandler(String executorHandler) {
		this.executorHandler = executorHandler;
	}

	public String getExecutorParam() {
		return executorParam;
	}

	public void setExecutorParam(String executorParam) {
		this.executorParam = executorParam;
	}

	public String getExecutorBlockStrategy() {
		return executorBlockStrategy;
	}

	public void setExecutorBlockStrategy(String executorBlockStrategy) {
		this.executorBlockStrategy = executorBlockStrategy;
	}

	public String getExecutorFailStrategy() {
		return executorFailStrategy;
	}

	public void setExecutorFailStrategy(String executorFailStrategy) {
		this.executorFailStrategy = executorFailStrategy;
	}

	public String getGlueType() {
		return glueType;
	}

	public void setGlueType(String glueType) {
		this.glueType = glueType;
	}

	public String getGlueSource() {
		return glueSource;
	}

	public void setGlueSource(String glueSource) {
		this.glueSource = glueSource;
	}

	public String getGlueRemark() {
		return glueRemark;
	}

	public void setGlueRemark(String glueRemark) {
		this.glueRemark = glueRemark;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getGlueUpdateTime() {
		return glueUpdateTime;
	}

	public void setGlueUpdateTime(String glueUpdateTime) {
		this.glueUpdateTime = glueUpdateTime;
	}

	public String getChildJobId() {
		return childJobId;
	}

	public void setChildJobId(String childJobId) {
		this.childJobId = childJobId;
	}

	public String getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String[] getResourceIdArray() {
		return resourceIdArray;
	}

	public void setResourceIdArray(String[] resourceIdArray) {
		this.resourceIdArray = resourceIdArray;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public int getFolderId() {
		return folderId;
	}

	public void setFolderId(int folderId) {
		this.folderId = folderId;
	}

	public PrestoParam getPrestoParam() {
		return prestoParam;
	}

	public void setPrestoParam(PrestoParam prestoParam) {
		this.prestoParam = prestoParam;
	}
}
