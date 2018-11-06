package com.xxl.job.admin.controller;

import com.alibaba.fastjson.JSON;
import com.xxl.job.admin.controller.interceptor.PermissionInterceptor;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.service.XxlJobService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class JobInfoControllerTest extends AbstractSpringMvcTest {

  private Cookie cookie;

  @Resource
  private XxlJobService xxlJobService;
  @Resource
  private XxlJobInfoDao xxlJobInfoDao;

  @Before
  public void login() throws Exception {
    MvcResult ret = mockMvc.perform(
        post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("userName", XxlJobAdminConfig.getAdminConfig().getLoginUsername())
            .param("password", XxlJobAdminConfig.getAdminConfig().getLoginPassword())
    ).andReturn();
    cookie = ret.getResponse().getCookie(PermissionInterceptor.LOGIN_IDENTITY_KEY);
  }

  @Test
  public void testAdd() throws Exception {
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
    parameters.add("jobGroup", "1");

    MvcResult ret = mockMvc.perform(
        post("/jobinfo/pageList")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            //.content(paramsJson)
            .params(parameters)
            .cookie(cookie)
    ).andReturn();

    System.out.println(ret.getResponse().getContentAsString());
  }

  @Test
  public void testGetNewestJob(){
    XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(1);
    System.out.println("=========");
    if (xxlJobInfo != null) {
      System.out.println(JSON.toJSONString(xxlJobInfo));
    }
    System.out.println("null");
    System.out.println("=========");
  }

}
