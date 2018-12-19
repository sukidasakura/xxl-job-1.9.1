package com.xxl.executor.test;

import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.rpc.netcom.NetComClientProxy;

import java.util.Date;

/**
 * executor-api client, test
 *
 * Created by xuxueli on 17/5/12.
 */
public class DemoJobHandlerTest {

    public static void main(String[] args) throws Exception {

        // param
        String jobHandler = "demoJobHandler";
        String params = "";

        // trigger data
        TriggerParam triggerParam = new TriggerParam();
        triggerParam.setJobId(54);
        triggerParam.setCustomParam(null);
        triggerParam.setExecutorHandler(jobHandler);
        triggerParam.setExecutorParams(params);
        triggerParam.setGlueSource("#!/bin/bash\n" +
                "java -cp {Test_测试资源1，勿删！.jar} ${className} ${readpath} ${writepath}");
//        triggerParam.setGlueUpdatetime(DateUtils.convertDateTime(new Date()));
        triggerParam.setLogId(1);
//        triggerParam.setLogDateTim(System.currentTimeMillis());

        // do remote trigger
        String accessToken = null;
        ExecutorBiz executorBiz = (ExecutorBiz) new NetComClientProxy(ExecutorBiz.class, "http://10.10.77.136:9998", null).getObject();
        ReturnT returnT = executorBiz.killPid(triggerParam);
        System.out.println(returnT.getCode());
//        ReturnT<String> runResult = executorBiz.run(triggerParam);
    }

}
