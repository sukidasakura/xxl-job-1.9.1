package com.xxl.job.admin.dao;

import com.alibaba.fastjson.JSON;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.core.util.DateTool;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationcontext-*.xml")
public class XxlJobLogDaoTest {

    @Resource
    private XxlJobLogDao xxlJobLogDao;

    @Test
    public void findMaxId(){

        System.out.println("====");
        System.out.println(xxlJobLogDao.findMaxId());
        System.out.println("====");

    }

    @Test
    public void test(){


        System.out.println("==========");
        System.out.println(xxlJobLogDao.getLogByLogId(40));
        System.out.println("============");
//        XxlJobLog log = new XxlJobLog();
//        log.setJobGroup(1);
//        log.setJobId(1);
//
//        int ret1 = xxlJobLogDao.save(log);
//        XxlJobLog dto = xxlJobLogDao.loadByLogId(log.getId());
//
//        log.setGlueType("1");
//        log.setTriggerTime(DateTool.convertDateTime(new Date()));
//        log.setTriggerCode(1);
//        log.setTriggerMsg("1");
//        log.setExecutorAddress("1");
//        log.setExecutorHandler("1");
//        log.setExecutorParam("1");
//        ret1 = xxlJobLogDao.updateTriggerInfo(log);
//        dto = xxlJobLogDao.loadByLogId(log.getId());
//
//
//        log.setHandleTime(DateTool.convertDateTime(new Date()));
//        log.setHandleCode(2);
//        log.setHandleMsg("2");
//        ret1 = xxlJobLogDao.updateHandleInfo(log);
//        dto = xxlJobLogDao.loadByLogId(log.getId());
//
//
//        List<Map<String, Object>> list2 = xxlJobLogDao.triggerCountByDay(DateUtils.addDays(new Date(), 30), new Date());
//
//        int ret4 = xxlJobLogDao.clearLog(1, 1, new Date(), 100);
//
//        int ret2 = xxlJobLogDao.delete(log.getJobId());
//
//        int ret3 = xxlJobLogDao.triggerCountByHandleCode(-1);
    }

}
