package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.core.util.DateTool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationcontext-*.xml")
public class XxlJobRegistryDaoTest {

    @Resource
    private XxlJobRegistryDao xxlJobRegistryDao;

    @Test
    public void test(){
        int ret = xxlJobRegistryDao.registryUpdate("g1", "k1", "v1");
        if (ret < 1) {
            ret = xxlJobRegistryDao.registrySave(1,"g1", "k1", "v1", DateTool.convertDateTime(new Date()));
        }

        List<XxlJobRegistry> list = xxlJobRegistryDao.findAll(1);

        int ret2 = xxlJobRegistryDao.removeDead(1);
    }

}
