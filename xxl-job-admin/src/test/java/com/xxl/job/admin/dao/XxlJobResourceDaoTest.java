package com.xxl.job.admin.dao;

import com.alibaba.fastjson.JSON;
import com.xxl.job.core.biz.model.ReturnT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static com.xxl.job.admin.core.schedule.XxlJobDynamicScheduler.xxlJobInfoDao;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2018年12月13日 10:49
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationcontext-*.xml")
public class XxlJobResourceDaoTest {

    @Resource
    private XxlJobResourceDao xxlJobResourceDao;
    @Resource
    private XxlJobInfoDao XxlJobInfoDao;

    @Test
    public void pageList1(){

        System.out.println("====");
        System.out.println(xxlJobResourceDao.fileNameExist("Jar.jarsagasg"));
        System.out.println("====");

    }

    @Test
    public void deleteTest(){

        int id = 17;
        List<Integer> usedResources = XxlJobInfoDao.getUsedResources();
        for (int item : usedResources){
            if (item == id) { // 如果资源是被使用中的话, 不允许删除
                System.out.println(("资源使用中, 不允许删除"));
            }
        }

        int result = xxlJobResourceDao.delete(id);
    }

    @Test
    public void getIdByFileName(){

        System.out.println("====");
        System.out.println(JSON.toJSONString(xxlJobResourceDao.getIdByFileName("data_crud_impl-1.1.00.170701-SNAPSHOT_asa.jar")));
        System.out.println("====");

    }


}
