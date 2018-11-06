package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobLog;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * job log
 * @author xuxueli 2016-1-12 18:03:06
 */
public interface XxlJobLogDao {
	// logStatus ==1（成功）2（失败）3（进行中）
	List<XxlJobLog> pageList(@Param("offset") int offset,
							 @Param("pagesize") int pagesize,
							 @Param("jobGroup") int jobGroup,
							 @Param("jobId") int jobId,
							 @Param("triggerTimeStart") Date triggerTimeStart,
							 @Param("triggerTimeEnd") Date triggerTimeEnd,
							 @Param("logStatus") int logStatus);

	int pageListCount(@Param("offset") int offset,
					  @Param("pagesize") int pagesize,
					  @Param("jobGroup") int jobGroup,
					  @Param("jobId") int jobId,
					  @Param("triggerTimeStart") Date triggerTimeStart,
					  @Param("triggerTimeEnd") Date triggerTimeEnd,
					  @Param("logStatus") int logStatus);

	// 查找记录数
	long counts();

	// 查找所有日志
	List<XxlJobLog> findAll(@Param("offset") int offset,
							@Param("pagesize") int pagesize);


	// 根据logId，获取调度备注
	XxlJobLog getLogByLogId(@Param("logId") int logId);



	// 根据日志id，加载任务的日志详细信息
	XxlJobLog loadByLogId(@Param("id") int id);

	List<XxlJobLog> loadByJobId(@Param("jobId") int jobId);

	int save(XxlJobLog xxlJobLog);

	int updateTriggerInfo(XxlJobLog xxlJobLog);

	int updateHandleInfo(XxlJobLog xxlJobLog);

	int delete(@Param("jobId") int jobId);

	int triggerCountByHandleCode(@Param("handleCode") int handleCode);

	List<Map<String, Object>> triggerCountByDay(@Param("from") Date from,
												@Param("to") Date to);

	int clearLog(@Param("jobGroup") int jobGroup,
				 @Param("jobId") int jobId,
				 @Param("clearBeforeTime") Date clearBeforeTime,
				 @Param("clearBeforeNum") int clearBeforeNum);

	XxlJobLog getCurrentLog(@Param("jobId") int jobId);

}
