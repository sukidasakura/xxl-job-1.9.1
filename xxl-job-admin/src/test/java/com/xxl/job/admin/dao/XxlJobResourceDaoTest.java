package com.xxl.job.admin.dao;

import com.alibaba.fastjson.JSON;
import com.xxl.job.admin.controller.JobResourceController;
import com.xxl.job.admin.core.model.XxlJobResource;
import com.xxl.job.core.biz.model.ReturnT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

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


    @Test
    public void uploadResource() throws IOException {
        XxlJobResource xxlJobResource = new XxlJobResource();
        xxlJobResource.setId(333);
        xxlJobResource.setContent(getBytes("G:/sparkstreaming.jar"));
        xxlJobResource.setFileName("testtttttt");
        xxlJobResource.setDescribe("a");
        xxlJobResource.setType("1");

        ReturnT returnT = new ReturnT();

        if (xxlJobResourceDao.fileNameExist(xxlJobResource.getFileName()) != 0){
            returnT.setCode(500);
            returnT.setMsg("文件已存在, 重新选择文件");
            System.out.println("=================");
            System.out.println(JSON.toJSONString(returnT));
            System.out.println("=================");
            return;
        }


        /////////////////////////////////////
        FutureTask<Integer> task =
                new FutureTask<>(new UploadThread(xxlJobResource));
        Thread thread = new Thread(task);

        // 等待15s的时间，若线程还未返回结果，则认为执行线程已阻塞。
        try {
            thread.start();
            int result = task.get(15, TimeUnit.SECONDS);
            if (result > 0){
                int resourceId = xxlJobResourceDao.getIdByFileName(xxlJobResource.getFileName()).getId();
                returnT.setCode(ReturnT.SUCCESS_CODE);
                returnT.setMsg("新增成功，返回resourceId");
                returnT.setContent(resourceId);
            } else {
                returnT.setCode(ReturnT.FAIL_CODE);
                returnT.setMsg("新增失败");
            }
        } catch (Exception e) {
            thread.interrupt();
            e.printStackTrace();
            System.out.println("请求超时");
            return;
        }

        System.out.println("=================");
        System.out.println(JSON.toJSONString(returnT));
        System.out.println("=================");

//        int result = xxlJobResourceDao.upload(xxlJobResource);
//        if (result > 0){
//            int resourceId = xxlJobResourceDao.getIdByFileName(xxlJobResource.getFileName()).getId();
//            returnT.setCode(ReturnT.SUCCESS_CODE);
//            returnT.setMsg("新增成功，返回resourceId");
//            returnT.setContent(resourceId);
//        } else {
//            returnT.setCode(ReturnT.FAIL_CODE);
//            returnT.setMsg("新增失败");
//        }
        return;

    }

    class UploadThread implements Callable<Integer> {
        private XxlJobResource xxlJobResource;

        public UploadThread(XxlJobResource xxlJobResource) {
            this.xxlJobResource = xxlJobResource;
        }

        @Override
        public Integer call() throws Exception {
            int result = xxlJobResourceDao.upload(xxlJobResource);
            return result;
        }
    }





    private byte[] getBytes(String filePath){
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}
