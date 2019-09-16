package com.xxl.job.admin.service;


import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * core job action for xxl-job
 * 
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface XxlJobService {

	/**
	 *
	 * @return
	 */
	List<XxlJobInfo> findAll(int start, int length);


	/**
	 * 所有的任务列表
	 * @return
	 */
	List<Map<String, Object>> allJobList();

	/**
	 * 获取任务的数量
	 * @return
	 */
	public long counts();

	/**
	 * 任务是否存在
	 * @param jobName
	 * @return
	 */
	public boolean isJobExist(String jobName);

	/**
	 * page list
	 *
	 * @param start
	 * @param length
	 * @param jobGroup
	 * @param jobDesc
	 * @param executorHandler
	 * @param filterTime
	 * @return
	 */
	public Map<String, Object> pageList(int start, int length, int jobGroup, String jobDesc, String executorHandler, String filterTime);

	/**
	 *
	 * @param start
	 * @param length
	 * @param jobGroup
	 * @param GlueType
	 * @return
	 */
	public Map<String, Object> pageListByGlueType(int start, int length, int jobGroup, String glueType);

	/**
	 * add job "新增任务"
	 *
	 * @param jobInfo
	 * @return
	 */
	public ReturnT<String> restAdd(XxlJobInfo jobInfo);

	/**
	 * add job "新增任务"
	 *
	 * @param jobInfo
	 * @return
	 */
	public ReturnT<String> add(XxlJobInfo jobInfo);


	/**
	 * 加载任务
	 * @param id
	 * @return
	 */
	XxlJobInfo loadById(int id);

	/**
	 * update job "编辑"(用于data_control)
	 *
	 * @param jobInfo
	 * @return
	 */
	public ReturnT<String> restUpdate(XxlJobInfo jobInfo);

	/**
	 * update job "编辑"
	 *
	 * @param jobInfo
	 * @return
	 */
	public ReturnT<String> update(XxlJobInfo jobInfo);

	/**
	 * remove job
	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> remove(int id);

	/**
	 * remove job by name
	 * @param jobName
	 * @return
	 */
	public ReturnT<String> removeByName(String jobName);

	/**
	 * pause job
	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> pause(int id);

	/**
	 * resume job
	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> resume(int id);

	/**
	 * trigger job
	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> triggerJobById(int id);

	/**
	 * 触发任务 trigger job
	 * @param xxlJobInfo
	 * @return
	 */
	ReturnT<String> triggerJobByInfo(XxlJobInfo xxlJobInfo);


	/**
	 * 停止正在运行中的任务
	 * @param id
	 * @return
	 */
	ReturnT<String> killRunningJob(int id);

	/**
	 * dashboard info
	 *
	 * @return
	 */
	public Map<String,Object> dashboardInfo();

	/**
	 * chart info
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public ReturnT<Map<String,Object>> chartInfo(Date startDate, Date endDate);

	/**
	 * 获取最新添加的一条任务信息
	 * @return
	 */
	public XxlJobInfo getNewestJob();

}
