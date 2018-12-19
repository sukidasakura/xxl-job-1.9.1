package com.xxl.job.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.admin.controller.interceptor.PermissionInterceptor;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobResource;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobResourceDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.log.XxlJobFileAppender;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class JobInfoControllerTest extends AbstractSpringMvcTest {

    private Cookie cookie;

    @Resource
    private XxlJobService xxlJobService;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    public XxlJobResourceDao xxlJobResourceDao;

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
    public void testGetNewestJob() {
        System.out.println("=========");

        int jobId = 41;
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
        // 自定义参数
        String customParam = jobInfo.getCustomParam();
        String gluesource = jobInfo.getGlueSource(); // 代码


        if (gluesource.contains("java ") && gluesource.contains(".jar")) {
            // 如果资源不为空, 替换代码中的资源名为资源路径
            if (resources != null) {
                for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
                    // 生成资源文件(资源文件可能有多个)
                    // resourceName = srcpath/resource/resource.jar

                    String resourceName = XxlJobFileAppender.getResourceSrcPath()
                            .concat("/")
                            .concat(String.valueOf(entry.getKey()));

                    // 把代码中，资源文件例如{spark_sql_Demo.jar}处替换为带路径的资源，如srcpath/resource/spark_sql_Demo.jar
                    // 由于每个执行器的资源存储路径不同，所以这部分的替换要在各个执行节点上执行，无法在调度中心中替换。
                    String replaceKey = "{" + entry.getKey() + "}";
                    gluesource = gluesource.replace(replaceKey, resourceName);
                }

                Map<Integer, String> paramIndexMap = new HashMap<>();
                int maxParamIndex;
                String maxParamValue = null;
                // 替换代码中的${customParam}参数
                if (customParam != null) {
                    JSONObject params = JSONObject.parseObject(customParam);
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        String key = "${" + entry.getKey() + "}";
                        String paramValue = String.valueOf(entry.getValue());

                        gluesource = gluesource.replace(key, paramValue);

                        int paramIndex = gluesource.indexOf(paramValue) + paramValue.length();
                        paramIndexMap.put(paramIndex, String.valueOf(entry.getValue()));
                    }

                    // 找出自定义参数中在最后面的那一个，得到值
                    Object[] paramIndexArray = paramIndexMap.keySet().toArray();
                    Arrays.sort(paramIndexArray);
                    maxParamIndex = Integer.parseInt(String.valueOf(paramIndexArray[paramIndexArray.length - 1]));
                    maxParamValue = paramIndexMap.get(maxParamIndex);

                }
                String startStr = "java ";
                String endStr;
                if (customParam == null) {
                    endStr = ".jar";
                } else {
                    endStr = maxParamValue;
                }

                int startIndex = gluesource.indexOf(startStr) - startStr.length();
                int endIndex = gluesource.indexOf(endStr) + endStr.length();

                if (startIndex != 0 && endIndex != 0) {
                    String jarExecuteString = gluesource.substring(startIndex, endIndex).substring(startStr.length());
                }
            }
        }


        System.out.println("=========");
    }

}
