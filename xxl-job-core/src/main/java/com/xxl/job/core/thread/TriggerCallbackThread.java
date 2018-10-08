package com.xxl.job.core.thread;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.executor.XxlJobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by xuxueli on 16/7/22.
 */
public class TriggerCallbackThread {
    private static Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);

    private static TriggerCallbackThread instance = new TriggerCallbackThread();

    public static TriggerCallbackThread getInstance() {
        return instance;
    }

    /**
     * job results callback queue
     */
    private LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<HandleCallbackParam>();

    public static void pushCallBack(HandleCallbackParam callback) {
        getInstance().callBackQueue.add(callback);
        logger.debug(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    /**
     * callback thread
     */
    private Thread triggerCallbackThread;
    private volatile boolean toStop = false;

    public void start() {

        // valid
        if (XxlJobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>> xxl-job, executor callback config fail, adminAddresses is null.");
            return;
        }

        triggerCallbackThread = new Thread(new Runnable() {

            @Override
            public void run() {

                // normal callback
                while (!toStop) {
                    try {
                        // LinkedBlockingQueue的take()获取并移除此队列的头部，如果没有元素则等待（阻塞），直到有元素将唤醒等待线程执行该操作
                        HandleCallbackParam callback = getInstance().callBackQueue.take();
                        if (callback != null) {

                            // callback list param
                            List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                            // LinkedBlockingQueue的drainTo()移除此队列中所有可用的元素，并将它们添加到给定collection中。
                            int drainToNum = getInstance().callBackQueue.drainTo(callbackParamList);
                            callbackParamList.add(callback);

                            // callback, will retry if error
                            if (callbackParamList != null && callbackParamList.size() > 0) {
                                doCallback(callbackParamList);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                // last callback
                try {
                    List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                    int drainToNum = getInstance().callBackQueue.drainTo(callbackParamList);
                    if (callbackParamList != null && callbackParamList.size() > 0) {
                        doCallback(callbackParamList);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info(">>>>>>>>>>> xxl-job, executor callback thread destory.");

            }
        });
        triggerCallbackThread.setDaemon(true);
        triggerCallbackThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        triggerCallbackThread.interrupt();
        try {
            triggerCallbackThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * do callback, will retry if error
     *
     * @param callbackParamList
     */
    private void doCallback(List<HandleCallbackParam> callbackParamList) {
        // callback, will retry if error 获取调度中心的adminBiz列表，在执行器启动的时候初始化的
        for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
            try {
                // 这里的adminBiz调用的callback方法，因为是通过NetComClientProxy 这个factoryBean创建的代理对象，
                // 在getObject方法中，最终是没有调用的目标类方法的invoke的。只是将目标类的方法名，参数，类名，等信息发送给调度中心了
                // 发送的地址调度中心的接口地址是 ：“调度中心IP/api” 这个接口。这个是在执行器启动的时候初始化设置好的。
                // 调度中心的API接口拿到请求之后，通过参数里面的类名，方法，参数，反射出来一个对象，然后invoke， 最终将结果写入数据库
                ReturnT<String> callbackResult = adminBiz.callback(callbackParamList);
                if (callbackResult != null && ReturnT.SUCCESS_CODE == callbackResult.getCode()) {
                    callbackResult = ReturnT.SUCCESS;
                    // 因为调度中心是集群式的，所以只要有一台机器返回success，那么就算成功，直接break
                    logger.info(">>>>>>>>>>> xxl-job callback success, callbackParamList:{}, callbackResult:{}", new Object[]{callbackParamList, callbackResult});
                    break;
                } else {
                    logger.info(">>>>>>>>>>> xxl-job callback fail, callbackParamList:{}, callbackResult:{}", new Object[]{callbackParamList, callbackResult});
                }
            } catch (Exception e) {
                logger.error(">>>>>>>>>>> xxl-job callback error, callbackParamList：{}", callbackParamList, e);
                //getInstance().callBackQueue.addAll(callbackParamList);
            }
        }
    }

}
