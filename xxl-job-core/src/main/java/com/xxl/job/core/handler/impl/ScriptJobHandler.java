package com.xxl.job.core.handler.impl;

import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.util.DateUtil;
import com.xxl.job.core.util.ScriptUtil;
import com.xxl.job.core.util.ShardingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * Created by xuxueli on 17/4/27.
 */
public class ScriptJobHandler extends IJobHandler {

    private Logger logger = LoggerFactory.getLogger(ScriptJobHandler.class);

    private int jobId;
    private String glueUpdatetime;
    private String gluesource;
    private GlueTypeEnum glueType;
    private String customParam;
    private Map<String, byte[]> resources;

    public ScriptJobHandler(int jobId, String glueUpdatetime, String gluesource,
                            GlueTypeEnum glueType, String customParam,
                            Map<String, byte[]> resources){
        this.jobId = jobId;
        this.glueUpdatetime = glueUpdatetime;
        this.gluesource = gluesource;
        this.glueType = glueType;
        this.customParam = customParam;
        this.resources = resources;
    }

    public String getGlueUpdatetime() {
        return glueUpdatetime;
    }

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        // glueType：GLUE_SHELL、GLUE_PYTHON
        if (!glueType.isScript()) {
            return new ReturnT<String>(IJobHandler.FAIL.getCode(), "glueType["+ glueType +"] invalid.");
        }

        // cmd
        String cmd = glueType.getCmd(); // shell脚本对应bash，python脚本对应python


        // 1.在执行器上生成jar等资源文件   2.替换代码中的资源名为资源路径
        for (Map.Entry<String, byte[]> entry : resources.entrySet()){
            // 生成资源文件(资源文件可能有多个)
            // resourceName = srcpath/resource/resource.jar

            String resourceName = XxlJobFileAppender.getResourceSrcPath()
                    .concat("/")
                    .concat(String.valueOf(entry.getKey()));
            if (new File(resourceName).exists()) {
                logger.info("=====resource exist: " + resourceName + "=======");
                ScriptUtil.deleteFile(resourceName); // 如果原来有资源文件，先删除，再生成新的资源文件
            }
            logger.info("=====resource make: " + resourceName + "=======");
            ScriptUtil.makeResourceFile(resourceName, entry.getValue()); // 资源名，资源内容

            // 把代码中，资源文件例如{spark_sql_Demo.jar}处替换为带路径的资源，如srcpath/resource/spark_sql_Demo.jar
            // 由于每个执行器的资源存储路径不同，所以这部分的替换要在各个执行节点上执行，无法在调度中心中替换。
            String replaceKey = "{" + entry.getKey() + "}";
            gluesource = gluesource.replace(replaceKey, resourceName);
        }

        // 替换代码中的${customParam}参数
        JSONObject params = JSONObject.parseObject(customParam);
        for (Map.Entry<String, Object> entry : params.entrySet()){
            String key = "${" + entry.getKey() + "}";
            gluesource = gluesource.replace(key, String.valueOf(entry.getValue()));
        }

        logger.info("=====gluesource:==== " + gluesource);
        // make script file 生成脚本文件
        // 格式为：srcpath/gluesource/1_1533800850000.sh
        String scriptFileName = XxlJobFileAppender.getGlueSrcPath()
                .concat("/")
                .concat(String.valueOf(jobId))
                .concat("_")
                .concat(DateUtil.convertToLongTime(glueUpdatetime))
                .concat(glueType.getSuffix());
        ScriptUtil.markScriptFile(scriptFileName, gluesource);


        // log file
        String logFileName = XxlJobFileAppender.contextHolder.get();

        // script params：0=param、1=分片序号、2=分片总数
        ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
        String[] scriptParams = new String[3];
        scriptParams[0] = param;
        scriptParams[1] = String.valueOf(shardingVO.getIndex());
        scriptParams[2] = String.valueOf(shardingVO.getTotal());

        // invoke
        XxlJobLogger.log("--------- script file:"+ scriptFileName +" --------");
        // 执行任务，并将结果输出到日志
        int exitValue = ScriptUtil.execToFile(cmd, scriptFileName, logFileName, scriptParams);
        ReturnT<String> result = (exitValue==0)?IJobHandler.SUCCESS:new ReturnT<String>(IJobHandler.FAIL.getCode(), "script exit value("+exitValue+") is failed");
        return result;
    }

}
