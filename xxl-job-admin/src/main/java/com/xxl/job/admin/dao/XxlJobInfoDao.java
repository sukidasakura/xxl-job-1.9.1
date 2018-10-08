package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * job info
 * @author xuxueli 2016-1-12 18:03:45
 */
public interface XxlJobInfoDao {

	/**
	 * 返回所有的任务列表（任务ID，任务描述）
	 * @return
	 */
	List<XxlJobInfo> allJobList();

	/**
	 * 返回指定页数的job列表
	 * @param offset 第几页开始
	 * @param pagesize 一页中放几条记录
	 * @param jobGroup 根据执行器来分页//也可以不加这一条
	 * @param jobDesc
	 * @param executorHandler
	 * @return
	 */
	List<XxlJobInfo> pageList(@Param("offset") int offset,
							  @Param("pagesize") int pagesize,
							  @Param("jobGroup") int jobGroup,
							  @Param("jobDesc") String jobDesc,
							  @Param("executorHandler") String executorHandler);

	/**
	 * 返回符合条件的行数：
	 * 1. 若jobGroup大于0， 能够在数据库中匹配到传入的jobGroup
	 * 2. 若jobDesc不为空，能够在数据库中模糊匹配到传入的jobDesc
	 * 3. 若executorHandler不为空，能够在数据库中模糊匹配到传入的executorHandler
	 * 三者是AND的关系
	 * @param offset
	 * @param pagesize
	 * @param jobGroup
	 * @param jobDesc
	 * @param executorHandler
	 * @return
	 */
	int pageListCount(@Param("offset") int offset,
					  @Param("pagesize") int pagesize,
					  @Param("jobGroup") int jobGroup,
					  @Param("jobDesc") String jobDesc,
					  @Param("executorHandler") String executorHandler);

	// 保存任务信息
	// 需要根据任务信息的入参来修改
	int save(XxlJobInfo info);

	// 根据任务id加载任务信息
	XxlJobInfo loadById(@Param("id") int id);

	// 更新任务信息
	int update(XxlJobInfo item);

	// 删除任务
	int delete(@Param("id") int id);

	// 根据执行器获取任务列表
	List<XxlJobInfo> getJobsByGroup(@Param("jobGroup") int jobGroup);

	// 获取所有任务的数量
	int findAllCount();

}
