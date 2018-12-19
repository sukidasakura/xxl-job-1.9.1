package com.xxl.job.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.xxl.job.admin.core.enums.ExecutorFailStrategyEnum;
import com.xxl.job.admin.core.model.*;
import com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum;
import com.xxl.job.admin.core.schedule.XxlJobDynamicScheduler;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.core.util.LocalCacheUtil;
import com.xxl.job.admin.dao.*;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.util.DateTool;
import com.xxl.job.core.util.ScriptUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;

/**
 * core job action for xxl-job
 *
 * @author xuxueli 2016-5-28 15:30:33
 */
@Service
public class XxlJobServiceImpl implements XxlJobService {
    private static Logger logger = LoggerFactory.getLogger(XxlJobServiceImpl.class);

    @Resource
    private XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    public XxlJobLogDao xxlJobLogDao;
    @Resource
    private XxlJobLogGlueDao xxlJobLogGlueDao;
    @Resource
    public XxlJobResourceDao xxlJobResourceDao;

    @Override
    public List<XxlJobInfo> findAll(int start, int length) {
        List<XxlJobInfo> list = xxlJobInfoDao.findAll(start, length);

        // fill job info
        if (list != null && list.size() > 0) {
            for (XxlJobInfo jobInfo : list) {
                XxlJobDynamicScheduler.fillJobInfo(jobInfo);
            }
        }

        return list;
    }

    /**
     * 返回所有任务列表(用于data_center)
     *
     * @return
     */
    @Override
    public List<Map<String, Object>> allJobList() {
        List<XxlJobInfo> list = xxlJobInfoDao.allJobList();
        List<Map<String, Object>> ret = new ArrayList<>();

        if (list != null && list.size() > 0) {
            for (XxlJobInfo jobInfo : list) {
                // fill job info 把任务信息添加到quartz的调度器中
                XxlJobDynamicScheduler.fillJobInfo(jobInfo);

                // 只返回给前端任务id 与name
                Map<String, Object> map = new HashMap<>();
                map.put("id", jobInfo.getId());
                map.put("job_name", jobInfo.getJobName());
                map.put("glue_type", jobInfo.getGlueType());
                ret.add(map);
            }
        }
        return ret;
    }

    /**
     * 获取任务的数量
     * @return
     */
    public long counts(){
        return xxlJobInfoDao.counts();
    }

    /**
     * 载入任务信息
     *
     * @param id
     * @return
     */
    @Override
    public XxlJobInfo loadById(int id) {

        XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
        if (xxlJobInfo == null) {
            return null;
        }
        return xxlJobInfo;

    }


    @Override
    public Map<String, Object> pageList(int start, int length, int jobGroup, String jobDesc, String executorHandler, String filterTime) {

        // page list
        List<XxlJobInfo> list = xxlJobInfoDao.pageList(start, length, jobGroup, jobDesc, executorHandler);
        int list_count = xxlJobInfoDao.pageListCount(start, length, jobGroup, jobDesc, executorHandler);

        // fill job info
        if (list != null && list.size() > 0) {
            for (XxlJobInfo jobInfo : list) {
                XxlJobDynamicScheduler.fillJobInfo(jobInfo);
            }
        }

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);        // 总记录数
        maps.put("recordsFiltered", list_count);    // 过滤后的总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

    @Override
    public Map<String, Object> pageListByGlueType(int start, int length, int jobGroup, String glueType) {
        // page list
        List<XxlJobInfo> list = xxlJobInfoDao.pageListByGlueType(start, length, jobGroup, glueType);
        int list_count = xxlJobInfoDao.pageListCountByGlueType(start, length, jobGroup, glueType);

        // fill job info
        if (list != null && list.size() > 0) {
            for (XxlJobInfo jobInfo : list) {
                XxlJobDynamicScheduler.fillJobInfo(jobInfo);
            }
        }

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);        // 总记录数
        maps.put("recordsFiltered", list_count);    // 过滤后的总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

    /**
     * 新增任务，新增任务时要选择执行器、输入调度时间与选择策略等等（用于data_control xxl）
     * 1.要将任务添加到数据库中
     * 2.要将任务添加到quartz中
     *
     * @param jobInfo
     * @return
     */
    @Override
    public ReturnT<String> restAdd(XxlJobInfo jobInfo) {
        // valid
        XxlJobGroup group = xxlJobGroupDao.load(jobInfo.getJobGroup());
        // 看选择的执行器是否可用
        if (group == null) {
            return new ReturnT<>(500, "请选择执行器");
        }
        if (StringUtils.isBlank(jobInfo.getJobName())) {
            return new ReturnT<>(500, "请输入任务名");
        }
        if (GlueTypeEnum.match(jobInfo.getGlueType().toUpperCase()) == null) {
            return new ReturnT<>(500, "运行模式非法");
        }
        jobInfo.setGlueType(jobInfo.getGlueType().toUpperCase());
//		if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
//			return new ReturnT<>(ReturnT.FAIL_CODE, "Cron格式非法" );
//		}


        // 添加任务时，设置默认的编辑代码。 fix "\r" in shell
        if (GlueTypeEnum.SHELL.name().equals(jobInfo.getGlueType())) {
            jobInfo.setGlueSource("#!/bin/bash\n"
                    + "echo \" hello shell\"");
            jobInfo.setGlueSource(jobInfo.getGlueSource().replaceAll("\r", ""));
        }

        if (GlueTypeEnum.PYTHON.name().equals(jobInfo.getGlueType())) {
            jobInfo.setGlueSource("#!/usr/bin/python\n" +
                    "# -*- coding: UTF-8 -*-\n" +
                    "import time\n" +
                    "import sys\n" +
                    "\n" +
                    "print (\"hello python\")");
            jobInfo.setGlueSource(jobInfo.getGlueSource().replaceAll("\r", ""));
        }
        jobInfo.setGlueUpdateTime(DateTool.convertDateTime(new Date()));

        jobInfo.setAddTime(DateTool.convertDateTime(new Date()));

        // add in db 添加到数据库中
        xxlJobInfoDao.save(jobInfo);
        if (jobInfo.getId() < 1) { // 新增任务失败
            return new ReturnT<String>(ReturnT.FAIL_CODE, "新增任务失败");
        }

        // add in quartz 添加到quartz中
        String qz_group = String.valueOf(jobInfo.getJobGroup());
        String qz_name = String.valueOf(jobInfo.getId());
        try {
            String cron = "0 15 10 15 * ?"; // 新增时没有调度周期，先设置默认的调度周期
            // 添加任务的这个时候，就已经有了任务的日志了
            XxlJobDynamicScheduler.addJob(qz_name, qz_group, cron);
            //XxlJobDynamicScheduler.pauseJob(qz_name, qz_group);
            return ReturnT.SUCCESS;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            try { // 如果添加失败的话，要删除在数据库以及quartz中的这部分数据
                xxlJobInfoDao.delete(jobInfo.getId());
                XxlJobDynamicScheduler.removeJob(qz_name, qz_group);
            } catch (SchedulerException e1) {
                logger.error(e.getMessage(), e1);
            }
            return new ReturnT<String>(ReturnT.FAIL_CODE, ("新增任务失败") + ":" + e.getMessage());
        }

    }





    /**
     * 更新/保存任务信息（用于data_control xxl）
     *
     * @param jobInfo
     * @return
     */
    @Override
    public ReturnT<String> restUpdate(XxlJobInfo jobInfo) {

        // valid
        if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("路由策略非法"));
        }
        if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("阻塞处理策略非法"));
        }
        if (ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("失败处理策略非法"));
        }
        if (StringUtils.isBlank(jobInfo.getGlueSource())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请输入代码"));
        }
        if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "Cron格式非法");
        }
        if (jobInfo.getJobGroup() == 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请选择执行器"));
        }
        if (jobInfo.getGlueType() == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请选择任务类型"));
        }


        // 资源是否可用
        if (StringUtils.isNotBlank(jobInfo.getResourceId())) {
            String[] resourceIds = StringUtils.split(jobInfo.getResourceId(), ",");
            for (String resourceIdItem : resourceIds) {
                if (StringUtils.isNotBlank(resourceIdItem) && StringUtils.isNumeric(resourceIdItem)) {
                    XxlJobResource xxlJobResource = xxlJobResourceDao.loadById(Integer.valueOf(resourceIdItem));
                    if (xxlJobResource == null) {
                        return new ReturnT<>(ReturnT.FAIL_CODE, "资源不存在");
                    }
                } else {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "资源不存在");
                }
            }
        }

        XxlJobInfo exists_jobInfo = xxlJobInfoDao.loadById(jobInfo.getId());
        if (exists_jobInfo == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "任务ID不存在");
        }
        int oldGroup = exists_jobInfo.getJobGroup(); // 未更新之前设置的执行器

        // 修改任务信息
        exists_jobInfo.setUpdateTime(DateTool.convertDateTime(new Date()));
        exists_jobInfo.setJobCron(jobInfo.getJobCron());
        exists_jobInfo.setJobDesc(jobInfo.getJobDesc());
        exists_jobInfo.setJobGroup(jobInfo.getJobGroup());
        exists_jobInfo.setAuthor(jobInfo.getAuthor());
        exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
        exists_jobInfo.setExecutorHandler(jobInfo.getExecutorHandler());
        exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
        exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
        exists_jobInfo.setExecutorFailStrategy(jobInfo.getExecutorFailStrategy());
        exists_jobInfo.setChildJobId(jobInfo.getChildJobId());
        exists_jobInfo.setGlueSource(jobInfo.getGlueSource());
        exists_jobInfo.setGlueUpdateTime(DateTool.convertDateTime(new Date()));
        exists_jobInfo.setCustomParam(jobInfo.getCustomParam());
        exists_jobInfo.setResourceId(jobInfo.getResourceId());
        exists_jobInfo.setGlueType(jobInfo.getGlueType().toUpperCase());
        xxlJobInfoDao.update(exists_jobInfo);

        // fresh quartz
        String qz_group = String.valueOf(exists_jobInfo.getJobGroup());
        String qz_name = String.valueOf(exists_jobInfo.getId());
        // 若在更新任务时修改了执行器job_group，需要把quartz中之前的任务删除，重新添加，再进行调度
        if (oldGroup != jobInfo.getJobGroup()) {
            try {
                XxlJobDynamicScheduler.removeJob(qz_name, String.valueOf(oldGroup));
                XxlJobDynamicScheduler.addJob(qz_name, qz_group, exists_jobInfo.getJobCron());
                return ReturnT.SUCCESS;
            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
                try { // 如果添加失败的话，要删除在数据库以及quartz中的这部分数据
                    xxlJobInfoDao.delete(jobInfo.getId());
                    XxlJobDynamicScheduler.removeJob(qz_name, qz_group);
                } catch (SchedulerException e1) {
                    logger.error(e.getMessage(), e1);
                }
                return new ReturnT<>(ReturnT.FAIL_CODE, "修改任务执行器失败: " + e.getMessage());
            }
        }

        try {
            boolean ret = XxlJobDynamicScheduler.rescheduleJob(qz_group, qz_name, exists_jobInfo.getJobCron());
            return ret ? ReturnT.SUCCESS : ReturnT.FAIL;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }

        return ReturnT.FAIL;

    }




    /**
     * 任务详情页面，调试任务，手动触发一次任务。
     * 要先把任务信息更新到数据库中，再执行。相当于执行前进行一次update操作
     *
     * @param xxlJobInfo
     * @return
     */
    @Override
    public ReturnT<String> triggerJobByInfo(XxlJobInfo xxlJobInfo) {
        // valid
        if (ExecutorRouteStrategyEnum.match(xxlJobInfo.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("路由策略非法"));
        }
        if (ExecutorBlockStrategyEnum.match(xxlJobInfo.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("阻塞处理策略非法"));
        }
        if (ExecutorFailStrategyEnum.match(xxlJobInfo.getExecutorFailStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("失败处理策略非法"));
        }
        if (StringUtils.isBlank(xxlJobInfo.getGlueSource())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请输入代码"));
        }
        if (xxlJobInfo.getJobGroup() == 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请选择执行器"));
        }
        if (xxlJobInfo.getGlueType() == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请选择任务类型"));
        }

        // 资源是否可用
        if (StringUtils.isNotBlank(xxlJobInfo.getResourceId())) {
            String[] resourceIds = StringUtils.split(xxlJobInfo.getResourceId(), ",");
            for (String resourceIdItem : resourceIds) {
                if (StringUtils.isNotBlank(resourceIdItem) && StringUtils.isNumeric(resourceIdItem)) {
                    XxlJobResource xxlJobResource = xxlJobResourceDao.loadById(Integer.valueOf(resourceIdItem));
                    if (xxlJobResource == null) {
                        return new ReturnT<>(ReturnT.FAIL_CODE, "资源ID: " + resourceIdItem + " 不存在");
                    }
                } else {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "资源ID: " + resourceIdItem + " 不存在");
                }
            }
        }

        // ChildJobId valid
        if (StringUtils.isNotBlank(xxlJobInfo.getChildJobId())) {
            String[] childJobIds = StringUtils.split(xxlJobInfo.getChildJobId(), ",");
            for (String childJobIdItem : childJobIds) {
                if (StringUtils.isNotBlank(childJobIdItem) && StringUtils.isNumeric(childJobIdItem)) {
                    XxlJobInfo childJobInfo = xxlJobInfoDao.loadById(Integer.valueOf(childJobIdItem));
                    if (childJobInfo == null) {
                        return new ReturnT<String>(ReturnT.FAIL_CODE,
                                MessageFormat.format(("子任务ID" + "({0})" + "不存在"), childJobIdItem));
                    }
                    // avoid cycle relate
                    if (childJobInfo.getId() == xxlJobInfo.getId()) {
                        return new ReturnT<String>(ReturnT.FAIL_CODE, MessageFormat.format(I18nUtil.getString("子任务ID({0})不可与父任务重复"), childJobIdItem));
                    }
                } else {
                    return new ReturnT<String>(ReturnT.FAIL_CODE,
                            MessageFormat.format((I18nUtil.getString("子任务ID") + "({0})" + I18nUtil.getString("不存在")), childJobIdItem));
                }
            }
            xxlJobInfo.setChildJobId(StringUtils.join(childJobIds, ","));
        }

        XxlJobInfo exists_jobInfo = xxlJobInfoDao.loadById(xxlJobInfo.getId());
        if (exists_jobInfo == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "任务ID不存在");
        }

        // 修改任务信息
        exists_jobInfo.setUpdateTime(DateTool.convertDateTime(new Date()));
        exists_jobInfo.setJobGroup(xxlJobInfo.getJobGroup());
        exists_jobInfo.setJobCron(xxlJobInfo.getJobCron());
        exists_jobInfo.setJobDesc(xxlJobInfo.getJobDesc());
        exists_jobInfo.setAuthor(xxlJobInfo.getAuthor());
        exists_jobInfo.setExecutorRouteStrategy(xxlJobInfo.getExecutorRouteStrategy());
        exists_jobInfo.setExecutorHandler(xxlJobInfo.getExecutorHandler());
        exists_jobInfo.setExecutorParam(xxlJobInfo.getExecutorParam());
        exists_jobInfo.setExecutorBlockStrategy(xxlJobInfo.getExecutorBlockStrategy());
        exists_jobInfo.setExecutorFailStrategy(xxlJobInfo.getExecutorFailStrategy());
        exists_jobInfo.setChildJobId(xxlJobInfo.getChildJobId());
        exists_jobInfo.setGlueSource(xxlJobInfo.getGlueSource());
        exists_jobInfo.setGlueUpdateTime(DateTool.convertDateTime(new Date()));
        exists_jobInfo.setCustomParam(xxlJobInfo.getCustomParam());
        exists_jobInfo.setResourceId(xxlJobInfo.getResourceId());
        exists_jobInfo.setGlueType(xxlJobInfo.getGlueType().toUpperCase());

        xxlJobInfoDao.update(exists_jobInfo);

        // fresh quartz
        String qz_group = String.valueOf(exists_jobInfo.getJobGroup());
        String qz_name = String.valueOf(exists_jobInfo.getId());
        try {
            boolean ret1 = XxlJobDynamicScheduler.rescheduleJob(qz_group, qz_name, exists_jobInfo.getJobCron());
            // 调用执行器类，触发该任务
            boolean ret2 = XxlJobDynamicScheduler.triggerJob(qz_name, qz_group);
            return (ret1 && ret2) ? ReturnT.SUCCESS : ReturnT.FAIL;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
        }
    }

    /**
     * 任务管理页面，根据任务ID触发任务
     *
     * @param jobId
     * @return
     */
    @Override
    public ReturnT<String> triggerJobById(int jobId) {
        // 从数据库中查询该任务的具体信息
        XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(jobId);
        if (xxlJobInfo == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "任务ID非法");
        }

        String group = String.valueOf(xxlJobInfo.getJobGroup());
        String name = String.valueOf(xxlJobInfo.getId());

        try {
            // 调用执行器类，触发该任务
            XxlJobDynamicScheduler.triggerJob(name, group);
            return ReturnT.SUCCESS;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
        }
    }


    /**
     * 停止正在运行中的任务
     *
     * @param jobId
     * @return
     */
    public ReturnT<String> killRunningJob(int jobId) {
        // 从数据库中获取该日志信息
        //  触发任务后，数据库的日志中就会多出最新的一条日志信息。根据job id来找到该jobId对应的最新的一条日志
        XxlJobLog log = xxlJobLogDao.getCurrentLog(jobId);
        // 获取该任务的信息
        XxlJobInfo jobInfo = xxlJobInfoDao.loadById(jobId);
        if (jobInfo == null) {
            return new ReturnT<>(500, "任务ID非法");
        }
        if (ReturnT.SUCCESS_CODE != log.getTriggerCode()) {
            // 任务没有触发成功，无需终止
            return new ReturnT<>(500, "调度失败，无法终止日志");
        }

        // request of kill
        ReturnT<String> runResult;
        try {
            // 通过NetComClientProxy创建代理对象，代理对象invoke方法里面包含了HTTP请求，会将该请求发送至执行器那一端。
            // 通过执行器来终止该任务，主要是执行器那边的kill方法。
            ExecutorBiz executorBiz = XxlJobDynamicScheduler.getExecutorBiz(log.getExecutorAddress());
            runResult = executorBiz.kill(jobInfo.getId());
            executorBiz.killPid(getTriggerParam(jobInfo.getId()));



        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            runResult = new ReturnT<>(500, e.getMessage());
        }

        if (ReturnT.SUCCESS_CODE == runResult.getCode()) {
            log.setHandleCode(ReturnT.FAIL_CODE);
            log.setHandleMsg("人为操作主动终止：" + (runResult.getMsg() != null ? runResult.getMsg() : ""));
            log.setHandleTime(DateTool.convertDateTime(new Date()));
            xxlJobLogDao.updateHandleInfo(log);
            return new ReturnT<>(runResult.getMsg());
        } else {
            return new ReturnT<>(500, runResult.getMsg());
        }
    }


    /**
     *
     * @param jobId
     */
    private TriggerParam getTriggerParam(int jobId){

        TriggerParam triggerParam = new TriggerParam();

        XxlJobInfo jobInfo = xxlJobInfoDao.loadById(jobId);

        Map<String, byte[]> resources = new HashMap<>();
        // 设置资源名和资源内容
        if (StringUtils.isNotBlank(jobInfo.getResourceId())) {
            String[] resourceIds = StringUtils.split(jobInfo.getResourceId(), ",");
            for (String resourceIdItem : resourceIds) {
                if (StringUtils.isNotBlank(resourceIdItem) && StringUtils.isNumeric(resourceIdItem)) {
                    XxlJobResource xxlJobResource = xxlJobResourceDao.loadById(Integer.valueOf(resourceIdItem));
                    resources.put(xxlJobResource.getFileName(), xxlJobResource.getContent());
                }
            }
        }
//        triggerParam.setJobId(jobInfo.getId());
//        triggerParam.setExecutorHandler(jobInfo.getExecutorHandler());
//        triggerParam.setExecutorParams(jobInfo.getExecutorParam());
//        triggerParam.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
//        triggerParam.setGlueType(jobInfo.getGlueType());
//        triggerParam.setGlueSource(jobInfo.getGlueSource());
//        triggerParam.setGlueUpdatetime(jobInfo.getGlueUpdateTime());
        // 自定义参数
        triggerParam.setCustomParam(jobInfo.getCustomParam());
        triggerParam.setGlueSource(jobInfo.getGlueSource());
        triggerParam.setResources(resources);
        triggerParam.setExecutorParams(jobInfo.getExecutorParam());
        return triggerParam;
    }


    /**
     * 根据任务id删除任务
     *
     * @param id
     * @return
     */
    @Override
    public ReturnT<String> remove(int id) {
        XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
        String group = String.valueOf(xxlJobInfo.getJobGroup());
        String name = String.valueOf(xxlJobInfo.getId());

        try {
            // 调用quartz删除他内置的定时器
            XxlJobDynamicScheduler.removeJob(name, group);
            // 删除数据库中的任务
            xxlJobInfoDao.delete(id);
            // 删除调度日志
            xxlJobLogDao.delete(id);
            // 如果是脚本类型的任务，则删除脚本变化日志
            xxlJobLogGlueDao.deleteByJobId(id);
            return ReturnT.SUCCESS;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
        return ReturnT.FAIL;
    }


    /**
     * 暂停调度
     *
     * @param id
     * @return
     */
    @Override
    public ReturnT<String> pause(int id) {
        // 从数据库中获取任务信息，主要是为了获取group和name， 这个是组成在quartz里面的定时器的key
        XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
        String group = String.valueOf(xxlJobInfo.getJobGroup());
        String name = String.valueOf(xxlJobInfo.getId());

        try {
            // 调用quartz操作类来暂停任务
            boolean ret = XxlJobDynamicScheduler.pauseJob(name, group);    // jobStatus do not store
            return ret ? ReturnT.SUCCESS : ReturnT.FAIL;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return ReturnT.FAIL;
        }
    }


    /**
     * 恢复调度
     *
     * @param id
     * @return
     */
    @Override
    public ReturnT<String> resume(int id) {
        // 从数据库中获取任务信息，主要是为了获取group和name， 这个是组成在quartz里面的定时器的key
        XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
        String group = String.valueOf(xxlJobInfo.getJobGroup());
        String name = String.valueOf(xxlJobInfo.getId());

        try {
            // 恢复任务
            boolean ret = XxlJobDynamicScheduler.resumeJob(name, group);
            return ret ? ReturnT.SUCCESS : ReturnT.FAIL;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return ReturnT.FAIL;
        }
    }


    @Override
    public Map<String, Object> dashboardInfo() {

        int jobInfoCount = xxlJobInfoDao.findAllCount();
        int jobLogCount = xxlJobLogDao.triggerCountByHandleCode(-1);
        int jobLogSuccessCount = xxlJobLogDao.triggerCountByHandleCode(ReturnT.SUCCESS_CODE);

        // executor count
        Set<String> executerAddressSet = new HashSet<String>();
        List<XxlJobGroup> groupList = xxlJobGroupDao.findAll();

        if (CollectionUtils.isNotEmpty(groupList)) {
            for (XxlJobGroup group : groupList) {
                if (CollectionUtils.isNotEmpty(group.getRegistryList())) {
                    executerAddressSet.addAll(group.getRegistryList());
                }
            }
        }

        int executorCount = executerAddressSet.size();

        Map<String, Object> dashboardMap = new HashMap<String, Object>();
        dashboardMap.put("jobInfoCount", jobInfoCount);
        dashboardMap.put("jobLogCount", jobLogCount);
        dashboardMap.put("jobLogSuccessCount", jobLogSuccessCount);
        dashboardMap.put("executorCount", executorCount);
        return dashboardMap;
    }

    private static final String TRIGGER_CHART_DATA_CACHE = "trigger_chart_data_cache";

    @Override
    public ReturnT<Map<String, Object>> chartInfo(Date startDate, Date endDate) {
        // get cache
        String cacheKey = TRIGGER_CHART_DATA_CACHE + "_" + startDate.getTime() + "_" + endDate.getTime();
        Map<String, Object> chartInfo = (Map<String, Object>) LocalCacheUtil.get(cacheKey);
        if (chartInfo != null) {
            return new ReturnT<Map<String, Object>>(chartInfo);
        }

        // process
        List<String> triggerDayList = new ArrayList<String>();
        List<Integer> triggerDayCountRunningList = new ArrayList<Integer>();
        List<Integer> triggerDayCountSucList = new ArrayList<Integer>();
        List<Integer> triggerDayCountFailList = new ArrayList<Integer>();
        int triggerCountRunningTotal = 0;
        int triggerCountSucTotal = 0;
        int triggerCountFailTotal = 0;

        List<Map<String, Object>> triggerCountMapAll = xxlJobLogDao.triggerCountByDay(startDate, endDate);
        if (CollectionUtils.isNotEmpty(triggerCountMapAll)) {
            for (Map<String, Object> item : triggerCountMapAll) {
                String day = String.valueOf(item.get("triggerDay"));
                int triggerDayCount = Integer.valueOf(String.valueOf(item.get("triggerDayCount")));
                int triggerDayCountRunning = Integer.valueOf(String.valueOf(item.get("triggerDayCountRunning")));
                int triggerDayCountSuc = Integer.valueOf(String.valueOf(item.get("triggerDayCountSuc")));
                int triggerDayCountFail = triggerDayCount - triggerDayCountRunning - triggerDayCountSuc;

                triggerDayList.add(day);
                triggerDayCountRunningList.add(triggerDayCountRunning);
                triggerDayCountSucList.add(triggerDayCountSuc);
                triggerDayCountFailList.add(triggerDayCountFail);

                triggerCountRunningTotal += triggerDayCountRunning;
                triggerCountSucTotal += triggerDayCountSuc;
                triggerCountFailTotal += triggerDayCountFail;
            }
        } else {
            for (int i = 4; i > -1; i--) {
                triggerDayList.add(FastDateFormat.getInstance("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -i)));
                triggerDayCountSucList.add(0);
                triggerDayCountFailList.add(0);
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("triggerDayList", triggerDayList);
        result.put("triggerDayCountRunningList", triggerDayCountRunningList);
        result.put("triggerDayCountSucList", triggerDayCountSucList);
        result.put("triggerDayCountFailList", triggerDayCountFailList);

        result.put("triggerCountRunningTotal", triggerCountRunningTotal);
        result.put("triggerCountSucTotal", triggerCountSucTotal);
        result.put("triggerCountFailTotal", triggerCountFailTotal);

        // set cache
        LocalCacheUtil.set(cacheKey, result, 60 * 1000);     // cache 60s

        return new ReturnT<Map<String, Object>>(result);
    }


    /**
     * 获取最新添加的任务
     * @return
     */
    public XxlJobInfo getNewestJob() {
        return xxlJobInfoDao.getNewestJob();
    }



    /**
     * 新增任务，新增任务时要选择执行器、输入调度时间与选择策略等等（用于原生xxl）
     * 1.要将任务添加到数据库中
     * 2.要将任务添加到quartz中
     *
     * @param jobInfo
     * @return
     */
    @Override
    public ReturnT<String> add(XxlJobInfo jobInfo) {
        // valid
        XxlJobGroup group = xxlJobGroupDao.load(jobInfo.getJobGroup());
        if (group == null) { //请选择执行器
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_choose") + I18nUtil.getString("jobinfo_field_jobgroup")));
        }
        if (StringUtils.isBlank(jobInfo.getJobName())) {
            if (StringUtils.isBlank(jobInfo.getJobDesc())) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_field_jobdesc")));
            }
            jobInfo.setJobName(jobInfo.getJobDesc());
        }
        if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("jobinfo_field_cron_unvalid")); //Cron格式非法
        }

        if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorRouteStrategy") + I18nUtil.getString("system_unvalid")));
        }
        if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorBlockStrategy") + I18nUtil.getString("system_unvalid")));
        }
        if (ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), null) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorFailStrategy") + I18nUtil.getString("system_unvalid")));
        }
        if (GlueTypeEnum.match(jobInfo.getGlueType()) == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_gluetype") + I18nUtil.getString("system_unvalid")));  //运行模式非法
        }
        if (GlueTypeEnum.BEAN == GlueTypeEnum.match(jobInfo.getGlueType()) && StringUtils.isBlank(jobInfo.getExecutorHandler())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input") + "JobHandler"));
        }

        // 添加任务时，设置默认的编辑代码。 fix "\r" in shell
        if (GlueTypeEnum.SHELL.name().equals(jobInfo.getGlueType())) {
            jobInfo.setGlueSource("#!/bin/bash\n"
                    + "echo \" hello shell\"");
            jobInfo.setGlueSource(jobInfo.getGlueSource().replaceAll("\r", ""));
        }

        if (GlueTypeEnum.PYTHON.name().equals(jobInfo.getGlueType())) {
            jobInfo.setGlueSource("#!/usr/bin/python\n" +
                    "# -*- coding: UTF-8 -*-\n" +
                    "import time\n" +
                    "import sys\n" +
                    "\n" +
                    "print (\"hello python\")");
            jobInfo.setGlueSource(jobInfo.getGlueSource().replaceAll("\r", ""));
        }
        jobInfo.setGlueUpdateTime(DateTool.convertDateTime(new Date()));

        // ChildJobId valid
        if (StringUtils.isNotBlank(jobInfo.getChildJobId())) {
            String[] childJobIds = StringUtils.split(jobInfo.getChildJobId(), ",");
            for (String childJobIdItem : childJobIds) {
                if (StringUtils.isNotBlank(childJobIdItem) && StringUtils.isNumeric(childJobIdItem)) {
                    XxlJobInfo childJobInfo = xxlJobInfoDao.loadById(Integer.valueOf(childJobIdItem));
                    if (childJobInfo == null) {
                        return new ReturnT<String>(ReturnT.FAIL_CODE,
                                MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId") + "({0})" + I18nUtil.getString("system_not_found")), childJobIdItem));
                    }
                } else {
                    return new ReturnT<String>(ReturnT.FAIL_CODE,
                            MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId") + "({0})" + I18nUtil.getString("system_unvalid")), childJobIdItem));
                }
            }
            jobInfo.setChildJobId(StringUtils.join(childJobIds, ","));
        }

        jobInfo.setAddTime(DateTool.convertDateTime(new Date()));
        // add in db 添加到数据库中
        xxlJobInfoDao.save(jobInfo);
        if (jobInfo.getId() < 1) { // 新增任务失败
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add") + I18nUtil.getString("system_fail")));
        }

        // add in quartz 添加到quartz中
        String qz_group = String.valueOf(jobInfo.getJobGroup());
        String qz_name = String.valueOf(jobInfo.getId());
        try {
            // 添加任务的这个时候，就已经有了任务的日志了
            XxlJobDynamicScheduler.addJob(qz_name, qz_group, jobInfo.getJobCron());
            //XxlJobDynamicScheduler.pauseJob(qz_name, qz_group);
            return ReturnT.SUCCESS;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            try { // 如果添加失败的话，要删除在数据库以及quartz中的这部分数据
                xxlJobInfoDao.delete(jobInfo.getId());
                XxlJobDynamicScheduler.removeJob(qz_name, qz_group);
            } catch (SchedulerException e1) {
                logger.error(e.getMessage(), e1);
            }
            return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add") + I18nUtil.getString("system_fail")) + ":" + e.getMessage()); //新增任务失败
        }
    }



    /**
     * 更新/保存任务信息（用于原生xxl）
     *
     * @param jobInfo
     * @return
     */
    @Override
    public ReturnT<String> update(XxlJobInfo jobInfo) {

        // valid
        if (StringUtils.isBlank(jobInfo.getAuthor())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请输入负责人"));
        }
        if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("路由策略非法"));
        }
        if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("阻塞处理策略非法"));
        }
        if (ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), null) == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("失败处理策略非法"));
        }
        if (StringUtils.isBlank(jobInfo.getGlueSource())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请输入代码"));
        }
        if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "Cron格式非法");
        }
        if (StringUtils.isBlank(String.valueOf(jobInfo.getJobGroup()))) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请选择执行器"));
        }
        if (jobInfo.getGlueType() != null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, ("请选择任务类型"));
        }


        // 资源是否可用
        if (StringUtils.isNotBlank(jobInfo.getResourceId())) {
            String[] resourceIds = StringUtils.split(jobInfo.getResourceId(), ",");
            for (String resourceIdItem : resourceIds) {
                if (StringUtils.isNotBlank(resourceIdItem) && StringUtils.isNumeric(resourceIdItem)) {
                    XxlJobResource xxlJobResource = xxlJobResourceDao.loadById(Integer.valueOf(resourceIdItem));
                    if (xxlJobResource == null) {
                        return new ReturnT<>(ReturnT.FAIL_CODE, "资源不存在");
                    }
                } else {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "资源不存在");
                }
            }
        }


        // ChildJobId valid
        if (StringUtils.isNotBlank(jobInfo.getChildJobId())) {
            String[] childJobIds = StringUtils.split(jobInfo.getChildJobId(), ",");
            for (String childJobIdItem : childJobIds) {
                if (StringUtils.isNotBlank(childJobIdItem) && StringUtils.isNumeric(childJobIdItem)) {
                    XxlJobInfo childJobInfo = xxlJobInfoDao.loadById(Integer.valueOf(childJobIdItem));
                    if (childJobInfo == null) {
                        return new ReturnT<String>(ReturnT.FAIL_CODE,
                                MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId") + "({0})" + I18nUtil.getString("system_not_found")), childJobIdItem));
                    }
                    // avoid cycle relate
                    if (childJobInfo.getId() == jobInfo.getId()) {
                        return new ReturnT<String>(ReturnT.FAIL_CODE, MessageFormat.format(I18nUtil.getString("jobinfo_field_childJobId_limit"), childJobIdItem));
                    }
                } else {
                    return new ReturnT<String>(ReturnT.FAIL_CODE,
                            MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId") + "({0})" + I18nUtil.getString("system_unvalid")), childJobIdItem));
                }
            }
            jobInfo.setChildJobId(StringUtils.join(childJobIds, ","));
        }

        XxlJobInfo exists_jobInfo = xxlJobInfoDao.loadById(jobInfo.getId());
        if (exists_jobInfo == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "任务ID不存在");
        }
        int oldGroup = exists_jobInfo.getJobGroup(); // 未更新之前设置的执行器

        // 修改任务信息
        exists_jobInfo.setUpdateTime(DateTool.convertDateTime(new Date()));
        exists_jobInfo.setJobCron(jobInfo.getJobCron());
        exists_jobInfo.setJobDesc(jobInfo.getJobDesc());
        exists_jobInfo.setJobGroup(jobInfo.getJobGroup());
        exists_jobInfo.setAuthor(jobInfo.getAuthor());
        exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
        exists_jobInfo.setExecutorHandler(jobInfo.getExecutorHandler());
        exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
        exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
        exists_jobInfo.setExecutorFailStrategy(jobInfo.getExecutorFailStrategy());
        exists_jobInfo.setChildJobId(jobInfo.getChildJobId());
        exists_jobInfo.setGlueSource(jobInfo.getGlueSource());
        exists_jobInfo.setGlueUpdateTime(DateTool.convertDateTime(new Date()));
        exists_jobInfo.setCustomParam(jobInfo.getCustomParam());
        exists_jobInfo.setResourceId(jobInfo.getResourceId());
        exists_jobInfo.setGlueType(jobInfo.getGlueType().toUpperCase());
        xxlJobInfoDao.update(exists_jobInfo);

        // fresh quartz
        String qz_group = String.valueOf(exists_jobInfo.getJobGroup());
        String qz_name = String.valueOf(exists_jobInfo.getId());
        // 若在更新任务时修改了执行器job_group，需要把quartz中之前的任务删除，重新添加，再进行调度
        if (oldGroup != jobInfo.getJobGroup()) {
            try {
                XxlJobDynamicScheduler.removeJob(qz_name, String.valueOf(oldGroup));
                XxlJobDynamicScheduler.addJob(qz_name, qz_group, exists_jobInfo.getJobCron());
                return ReturnT.SUCCESS;
            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
                try { // 如果添加失败的话，要删除在数据库以及quartz中的这部分数据
                    xxlJobInfoDao.delete(jobInfo.getId());
                    XxlJobDynamicScheduler.removeJob(qz_name, qz_group);
                } catch (SchedulerException e1) {
                    logger.error(e.getMessage(), e1);
                }
                return new ReturnT<>(ReturnT.FAIL_CODE, "修改任务执行器失败: " + e.getMessage());
            }
        }

        try {
            boolean ret = XxlJobDynamicScheduler.rescheduleJob(qz_group, qz_name, exists_jobInfo.getJobCron());
            return ret ? ReturnT.SUCCESS : ReturnT.FAIL;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }

        return ReturnT.FAIL;
    }
}
