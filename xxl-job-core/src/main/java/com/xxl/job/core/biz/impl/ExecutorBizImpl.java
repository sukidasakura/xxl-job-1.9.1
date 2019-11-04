package com.xxl.job.core.biz.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.supconit.data.crud.services.CrudAccessService;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.impl.GlueJobHandler;
import com.xxl.job.core.handler.impl.PrestoJobHandler;
import com.xxl.job.core.handler.impl.ScriptJobHandler;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.thread.JobThread;
import com.xxl.job.core.util.ScriptUtil;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.exec.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行executorBiz的run 方法的时候，首先会通过JobID，从本地线程库里面获取该任务对应的线程，
 * 同时，如果任务的JobHandler有更新的话，那么会自动使用最新的jobHandler，
 * 同时根据任务的阻塞策略执行不同的操作。
 * 最终，如果是第一次执行任务的时候，系统会分配给该任务一个线程，同时启动该线程。
 * 接下来，可以在具体看一下JobThread 的run方法，看下最终的任务是如何执行的。
 * Created by xuxueli on 17/3/1.
 */
public class ExecutorBizImpl implements ExecutorBiz {
    private static Logger logger = LoggerFactory.getLogger(ExecutorBizImpl.class);

    private CrudAccessService crudAccessService;

    public ExecutorBizImpl(CrudAccessService crudAccessService) {
        this.crudAccessService = crudAccessService;
    }

    @Override
    public ReturnT<String> beat() {
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> idleBeat(int jobId) {

        // isRunningOrHasQueue
        boolean isRunningOrHasQueue = false;
        // 从线程池里面获取当前任务对应的线程
        JobThread jobThread = XxlJobExecutor.loadJobThread(jobId);
        if (jobThread != null && jobThread.isRunningOrHasQueue()) {
            // 线程处于运行中
            isRunningOrHasQueue = true;
        }

        if (isRunningOrHasQueue) {
            // 线程运行中，则返回fasle
            return new ReturnT<String>(ReturnT.FAIL_CODE, "job thread is running or has trigger queue.");
        }
        // 线程空闲，返回success
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> kill(int jobId) {
        // kill handlerThread, and create new one 从线程池里面根据该任务ID，获取对应的线程
        JobThread jobThread = XxlJobExecutor.loadJobThread(jobId);
        if (jobThread != null) {
            // 线程存在，则手动移除，下面可以看一下removeJobThread方法
            XxlJobExecutor.removeJobThread(jobId, "人工手动终止");
            return ReturnT.SUCCESS;
        }

        return new ReturnT<String>(ReturnT.SUCCESS_CODE, "job thread aleady killed.");
    }

    /**
     * 如果任务中有java -java -cp {XXX.jar} ${className} ${readpath} ${writepath} 这样的语句
     * kill掉任务在linux后台对应的pid
     *
     * @param triggerParam
     * @return
     */
    public ReturnT<String> killPid(TriggerParam triggerParam) {

        // 获取到该任务的资源、源码、参数
        Map<String, byte[]> resources = triggerParam.getResources();
        String gluesource = triggerParam.getGlueSource();
        String customParam = triggerParam.getCustomParam();

        // 如果任务中有 java -cp resource.jar 1 2 的类似信息，获取到该句信息。（没有考虑有多句执行）
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
                    endStr = ".jar"; // 如果代码中不带参数，那么截取java 和.jar之间的这一句
                } else {
                    endStr = maxParamValue;
                }

                int startIndex = gluesource.indexOf(startStr) - startStr.length();
                int endIndex = gluesource.indexOf(endStr) + endStr.length();

                String jarExecuteString = null;
                if (startIndex != 0 && endIndex != 0) {
                    jarExecuteString = gluesource.substring(startIndex, endIndex).substring(startStr.length());
                    logger.info("jarExecuteString:" + jarExecuteString);
                }
                if (jarExecuteString != null) {
                    // 生成kill 任务的脚本
                    String killScriptName = XxlJobFileAppender.getKillScriptPath()
                            .concat("/")
                            .concat("kill")
                            .concat("_")
                            .concat(String.valueOf(System.currentTimeMillis()))
                            .concat(".sh");

                    if (new File(killScriptName).exists()) {
                        logger.info("[ killScriptName exist: " + killScriptName + " ] ");
                    }
                    logger.info("[ killScriptName make: " + killScriptName + " ]");

                    String killScriptContent = "echo \"kill start.\"\n" +
                            "pid=`ps x | grep \"" + jarExecuteString + "\" | awk '{print $1}'`\n" +
                            "kill $pid\n";
                    System.out.println(killScriptContent);

                    String logFileName = XxlJobFileAppender.contextHolder.get();
                    try {
                        ScriptUtil.markScriptFile(killScriptName, killScriptContent); // 生成killScript文件
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    // 执行脚本，kill linux后台运行的这个任务
                    try {
                        CommandLine commandLine = new CommandLine("bash");
                        commandLine.addArgument(killScriptName);// 拼接的commandline命令为："bash scriptFile"
                        DefaultExecutor exec = new DefaultExecutor();
                        exec.setExitValues(null);

                        int exitValue = exec.execute(commandLine);  // exit code: 0=success, 1=error
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    XxlJobLogger.log("<br> <br>");

                }
            }
        }
        return new ReturnT<String>(ReturnT.SUCCESS_CODE, "job pid is killed.");

    }

    @Override
    public ReturnT<LogResult> log(String logDateTim, int logId, int fromLineNum) {
        // log filename: logPath/yyyy-MM-dd/9999.log
        String logFileName = XxlJobFileAppender.makeLogFileName(logDateTim, logId);

        LogResult logResult = XxlJobFileAppender.readLog(logFileName, fromLineNum);
        return new ReturnT<LogResult>(logResult);
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        // load old：jobHandler + jobThread
        // 通过参数中的JobID， 从本地线程库里面获取线程 ( 第一次进来是没有线程的，jobThread为空 ，
        // 如果线程运行90秒空闲之后，那么也会被移除)
        // 本地线程库，本质上就是一个ConcurrentHashMap<Integer, JobThread>
        JobThread jobThread = XxlJobExecutor.loadJobThread(triggerParam.getJobId());
        IJobHandler jobHandler = jobThread != null ? jobThread.getHandler() : null;
        String removeOldReason = null;

        // valid：jobHandler + jobThread
        // 匹配任务类型，BEAN是我们自定义JOBHANDLE的模式
        GlueTypeEnum glueTypeEnum = GlueTypeEnum.match(triggerParam.getGlueType());
        if (GlueTypeEnum.BEAN == glueTypeEnum) {

            // new jobhandler
            // 通过参数中的handlerName从本地内存中获取handler实例
            // （在执行器启动的时候，是把所有带有@JobHandler的实例通过name放入到一个map中的 ）
            IJobHandler newJobHandler = XxlJobExecutor.loadJobHandler(triggerParam.getExecutorHandler());

            // valid old jobThread
            // 如果修改了任务的handler， name此处会默认把以前老的handler清空，后面会以最新的newJobHandler为准
            if (jobThread != null && jobHandler != newJobHandler) {
                // change handler, need kill old thread
                removeOldReason = "更换JobHandler或更换任务模式,终止旧任务线程";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                jobHandler = newJobHandler;
                if (jobHandler == null) {
                    return new ReturnT<String>(ReturnT.FAIL_CODE, "job handler [" + triggerParam.getExecutorHandler() + "] not found.");
                }
            }

        } else if (GlueTypeEnum.PRESTO == glueTypeEnum) {
            // 当任务类型为presto时，以自定义JOBHANDLE的模式运行

            // valid old jobThread
            if (jobThread != null &&
                    !(jobThread.getHandler() instanceof PrestoJobHandler
                            && ((PrestoJobHandler) jobThread.getHandler()).getGlueUpdatetime() == triggerParam.getGlueUpdatetime())) {
                removeOldReason = "更新任务逻辑或更换任务模式,终止旧任务线程";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                try {
                    System.out.println("~~~~~~~~~~~~~~");
                    System.out.println("triggerParam: " + JSON.toJSONString(triggerParam));
                    System.out.println("triggerParam.getPrestoParam(): " + JSON.toJSONString(triggerParam.getPrestoParam()));
                    System.out.println("~~~~~~~~~~~~~~");
                    jobHandler = new PrestoJobHandler(triggerParam.getGlueUpdatetime(),
                            triggerParam.getPrestoParam(), crudAccessService);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
                }
            }

        } else if (GlueTypeEnum.GLUE_GROOVY == glueTypeEnum) {
            // 此处说的是，任务模式为 GLUE JAVA版， 最后是通过GROOVY的方式，将代码生成class类，最终执行，最终原理和上面一致

            // valid old jobThread
            if (jobThread != null &&
                    !(jobThread.getHandler() instanceof GlueJobHandler
                            && ((GlueJobHandler) jobThread.getHandler()).getGlueUpdatetime() == triggerParam.getGlueUpdatetime())) {
                // change handler or gluesource updated, need kill old thread
                removeOldReason = "更新任务逻辑或更换任务模式,终止旧任务线程";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                try {
                    IJobHandler originJobHandler = GlueFactory.getInstance().loadNewInstance(triggerParam.getGlueSource());
                    jobHandler = new GlueJobHandler(originJobHandler, triggerParam.getGlueUpdatetime());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return new ReturnT<String>(ReturnT.FAIL_CODE, e.getMessage());
                }
            }
        } else if (glueTypeEnum != null && glueTypeEnum.isScript()) {
            // 其他脚本执行模式，shell , python, hive等

            logger.info("==== glueType is script!====");
            // valid old jobThread
            if (jobThread != null &&
                    !(jobThread.getHandler() instanceof ScriptJobHandler
                            && ((ScriptJobHandler) jobThread.getHandler()).getGlueUpdatetime() == triggerParam.getGlueUpdatetime())) {
                logger.info("=========更换任务模式终止旧线程=======");
                // change script or gluesource updated, need kill old thread
                removeOldReason = "更新任务逻辑或更换任务模式,终止旧任务线程";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                logger.info("=========jobHandler is null, create new ScriptJobHandler with resources and customparam! =======");
                jobHandler = new ScriptJobHandler(triggerParam.getJobId(),
                        triggerParam.getGlueUpdatetime(),
                        triggerParam.getGlueSource(),
                        GlueTypeEnum.match(triggerParam.getGlueType()),
                        triggerParam.getCustomParam(),
                        triggerParam.getResources());
            }
        } else {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "glueType[" + triggerParam.getGlueType() + "] is not valid.");
        }

        // executor block strategy
       /* 阻塞处理策略：调度过于密集执行器来不及处理时的处理策略；
        单机串行（默认）：调度请求进入单机执行器后，调度请求进入FIFO队列并以串行方式运行；
        丢弃后续调度：调度请求进入单机执行器后，发现执行器存在运行的调度任务，本次请求将会被丢弃并标记为失败；
        覆盖之前调度：调度请求进入单机执行器后，发现执行器存在运行的调度任务，将会终止运行中的调度任务并清空队列，然后运行本地调度任务；
        */
        if (jobThread != null) {
            ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(triggerParam.getExecutorBlockStrategy(), null);
            if (ExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
                // discard when running 这种阻塞策略说的是，丢弃后续调度，如果这个线程正在执行的话，那么当前这个任务就不执行了，直接返回
                if (jobThread.isRunningOrHasQueue()) {
                    return new ReturnT<String>(ReturnT.FAIL_CODE, "阻塞处理策略-生效：" + ExecutorBlockStrategyEnum.DISCARD_LATER.getTitle());
                }
            } else if (ExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy) {
                // kill running jobThread 覆盖之前的调度，如果当前线程已经执行了的话，那么中断这个线程， 直接将jobThread销毁
                if (jobThread.isRunningOrHasQueue()) {
                    removeOldReason = "阻塞处理策略-生效：" + ExecutorBlockStrategyEnum.COVER_EARLY.getTitle();

                    jobThread = null;
                }
            } else {
                // just queue trigger
            }
        }

        // replace thread (new or exists invalid)
        // 如果jobThread为空，那么这个时候，就要注册一个线程到本地线程库里面去。同时启动这个线程。
        if (jobThread == null) {
            jobThread = XxlJobExecutor.registJobThread(triggerParam.getJobId(), jobHandler, removeOldReason);
        }

        // push data to queue
        // 将本次任务的参数，放入到队列里面去，供线程调度。
        ReturnT<String> pushResult = jobThread.pushTriggerQueue(triggerParam);
        return pushResult;
    }

}
