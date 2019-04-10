package com.xxl.job.admin.dao;

import com.alibaba.fastjson.JSON;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobResource;
import com.xxl.job.core.util.DateTool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationcontext-*.xml")
public class XxlJobInfoDaoTest {
	
	@Resource
	private XxlJobInfoDao xxlJobInfoDao;

	@Test
	public void pageList1(){

		System.out.println(xxlJobInfoDao.allJobList());


//		List<XxlJobInfo> list = xxlJobInfoDao.pageList(0, 20, 0, null, null);
//		int list_count = xxlJobInfoDao.pageListCount(0, 20, 0, null, null);
//
//		System.out.println(list);
//		System.out.println(list_count);
//
//		List<XxlJobInfo> list2 = xxlJobInfoDao.getJobsByGroup(1);
	}
	
	@Test
	public void save_load(){
		XxlJobInfo info = new XxlJobInfo();
		info.setJobGroup(1);
		info.setJobCron("jobCron");
		info.setJobName("jobName");
		info.setJobDesc("desc");
		info.setAuthor("setAuthor");
		info.setAlarmEmail("setAlarmEmail");
		info.setExecutorRouteStrategy("setExecutorRouteStrategy");
		info.setExecutorHandler("setExecutorHandler");
		info.setExecutorParam("setExecutorParam");
		info.setExecutorBlockStrategy("setExecutorBlockStrategy");
		info.setExecutorFailStrategy("setExecutorFailStrategy");
		info.setGlueType("setGlueType");
		info.setGlueSource("setGlueSource");
		info.setGlueRemark("setGlueRemark");
		info.setChildJobId("1");

		int count = xxlJobInfoDao.save(info);

		XxlJobInfo info2 = xxlJobInfoDao.loadById(info.getId());
		info2.setJobCron("jobCron2");
		info.setJobName("jobName");
		info2.setJobDesc("desc2");
		info2.setAuthor("setAuthor2");
		info2.setAlarmEmail("setAlarmEmail2");
		info2.setExecutorRouteStrategy("setExecutorRouteStrategy2");
		info2.setExecutorHandler("setExecutorHandler2");
		info2.setExecutorParam("setExecutorParam2");
		info2.setExecutorBlockStrategy("setExecutorBlockStrategy2");
		info2.setExecutorFailStrategy("setExecutorFailStrategy2");
		info2.setGlueType("setGlueType2");
		info2.setGlueSource("setGlueSource2");
		info2.setGlueRemark("setGlueRemark2");
		info2.setGlueUpdateTime(DateTool.convertDateTime(new Date()));
		info2.setChildJobId("1");

		int item2 = xxlJobInfoDao.update(info2);

		xxlJobInfoDao.delete(info2.getId());

		List<XxlJobInfo> list2 = xxlJobInfoDao.getJobsByGroup(1);

		int ret3 = xxlJobInfoDao.findAllCount();

	}

	@Test
	public void pageList(){
		// page list
		System.out.println("==========");
		List<XxlJobInfo> list = xxlJobInfoDao.pageList(0, 10, 1, "", "");
//		List<XxlJobResource> list2 = xxlJobResourceDao.pageList(0, 1, "", "", "");

		System.out.println(JSON.toJSONString(list));
		System.out.println("==========");
	}

	@Test
	public void pageList2(){
		// page list
		System.out.println("==========");
		List<XxlJobInfo> list = xxlJobInfoDao.pageListByGlueType(0, 50, 1, "PYTHON");
//		List<XxlJobResource> list2 = xxlJobResourceDao.pageList(0, 1, "", "", "");

		for (XxlJobInfo i : list){
			System.out.println(i.getId() + i.getGlueType());
		}
		System.out.println("=====");

//		int a = xxlJobInfoDao.pageListCountByGlueType(1, 1, 1, "SHELL");
//		System.out.println(a);
//
//		System.out.println("==========");
	}


	@Test
	public void pageList3(){
		System.out.println("==========");
		XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(41);
		System.out.println(xxlJobInfo.getGlueSource());
		System.out.println("=====");
	}

	@Test
	public void loadByNameTest(){
		System.out.println("==========");
		XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadByName("test_deletehisfiles1111");
		System.out.println(JSON.toJSONString(xxlJobInfo));
		System.out.println("=====");
	}

	@Test
	public void isJobExistTest(){
		System.out.println("==========");
		if (xxlJobInfoDao.loadByName("chaizezhao") != null){ // 任务已存在
			System.out.println("true");
		} else {
			System.out.println("false");
		}
		System.out.println("=====");
	}


}
