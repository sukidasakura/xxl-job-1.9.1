package com.xxl.job.core.handler.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.supconit.data.crud.services.CrudAccessService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.entity.presto.*;
import com.xxl.job.core.entity.presto.catalog.DataContainer;
import com.xxl.job.core.entity.presto.catalog.DataElement;
import com.xxl.job.core.entity.presto.catalog.DataItem;
import com.xxl.job.core.entity.presto.catalog.DataAccessBackResult;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.util.FieldUtil;
import com.xxl.job.core.util.HttpClientUtil;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月15日 15:58
 */
@Service
public class PrestoJobHandler extends IJobHandler{

    private CrudAccessService crudAccessService;

    private String glueUpdatetime;

    private static Connection con;
    private String prestoAPI;
    private String dataManageAddress;

    private String datasource;
    private int jobId;
    private String title;
    private String query;

    private int save2db; // 是否将查询结果定时存储到数据库
    private Long itemId; // 数据容器接口需要的ID

    public PrestoJobHandler() {
    }

    public PrestoJobHandler(String glueUpdatetime,
                     PrestoParam prestoParam, CrudAccessService crudAccessService){
        System.out.println("PrestoJobHandler PrestoParam: " + prestoParam);
        this.glueUpdatetime = glueUpdatetime;
        this.prestoAPI = "http://" + prestoParam.getYanagishimaAddress();
        this.datasource = prestoParam.getDataSource();
        this.jobId = prestoParam.getJobId();
        this.title = prestoParam.getTitle();
        this.query = prestoParam.getQuery();
        this.save2db = prestoParam.getSave2db();
        this.itemId = prestoParam.getItemId();
        this.dataManageAddress = prestoParam.getDataManageAddress();
        this.crudAccessService = crudAccessService;

        try {
            Class.forName(prestoParam.getPrestoDbDriver());
            con = DriverManager.getConnection(prestoParam.getPrestoDbJdbcUrl(), prestoParam.getPrestoDbUsername(), prestoParam.getPrestoDbPassword());
            con.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public String getGlueUpdatetime() {
        return glueUpdatetime;
    }

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        String url = prestoAPI + "/prestoAsync";
        String paramA = "datasource=" + datasource + "&query=" + query;
        String queryIdJson = HttpClientUtil.getInstance().sendHttpPost(url, paramA);
        Map<String, String> queryIdMap = JSON.parseObject(queryIdJson, new TypeReference<Map<String, String>>() {
        });
        String queryId = queryIdMap.get("queryid");
        XxlJobLogger.log("<br> [ presto queryId: " + queryId + "  ] <br>");

        // 异步调用，不影响返回的结果
        MyCallable callable = new MyCallable(jobId, title, queryId, save2db, itemId);
        FutureTask<String> futureTask = new FutureTask<String>(callable);
        new Thread(futureTask).start();
        XxlJobLogger.log("<br> [ task return : " + Integer.valueOf(futureTask.get()) + "  ] <br><br>");
        int exitValue = Integer.valueOf(futureTask.get());

        ReturnT<String> result = (exitValue==0) ? IJobHandler.SUCCESS : new ReturnT<String>(IJobHandler.FAIL.getCode(), "task exit value(" + exitValue + ") is failed");
        XxlJobLogger.log("<br> [ result: " + result + "  ] <br>");

        return result;
    }

    public class MyCallable implements Callable<String> {

        private int id;
        private String title;
        private String queryId;
        private int save2db; // 是否将查询结果定时存储到数据库，1=是，0=否
        private Long itemId; // 数据容器接口需要的ID

        public MyCallable(int id, String title, String queryId, int save2db, Long itemId) {
            this.id = id;
            this.title = title;
            this.queryId = queryId;
            this.save2db = save2db;
            this.itemId = itemId;
        }

        @Override
        public String call() throws Exception {
            // 循环获取sql执行的状态，直到state != planning，意味着执行已完成
            String queryStatusUrl = prestoAPI + "/queryStatus";
            String queryStatusParam = "datasource=" + datasource + "&queryid=" + queryId;
            QueryStatus queryStatus;
            do {
                String queryStatusJson = HttpClientUtil.getInstance().sendHttpPost(queryStatusUrl, queryStatusParam);
                queryStatus = JSON.parseObject(queryStatusJson, new TypeReference<QueryStatus>() {});
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (queryStatus.getState().equals("PLANNING"));


            return saveLogToDatabase(id, title, queryId, save2db, itemId);
        }
    }


    public class batchCreateCallable implements Callable<String> {

        private String json;
        private Long itemId; // 数据容器接口需要的ID
        private String dataManageAddress;

        public batchCreateCallable(String json,
                                   Long itemId,
                                   String dataManageAddress) {
            this.json = json;
            this.itemId = itemId;
            this.dataManageAddress = dataManageAddress;
        }

        @Override
        public String call() throws Exception {
            PrestoResults prestoResults = JSON.parseObject(json, new TypeReference<PrestoResults>() {});

            if (prestoResults.getError() == null) { // 运行结果正确
                String[][] results = prestoResults.getResults();
                System.out.println(JSON.toJSONString(results));

                // 根据itemId获取业务库中有哪些字段
                String itemJson = HttpClientUtil.getInstance().sendHttpGet(5000,
                        dataManageAddress + "/catalog/item/elements/" + itemId);
                Map<String, List<DataElement>> dataElementMap = JSON.parseObject(itemJson,
                        new TypeReference<Map<String, List<DataElement>>>() {});

                System.out.println("dataElementMap: " + JSON.toJSONString(dataElementMap));

                // 根据itemId获取业务库信息
                String containerJson = HttpClientUtil.getInstance().sendHttpGet(5000,
                        dataManageAddress + "/catalog/item/containers/" + itemId);
                List<DataContainer> dataContainerList = JSON.parseObject(containerJson,
                        new TypeReference<List<DataContainer>>() {});

                System.out.println("containerJson: " + JSON.toJSONString(containerJson));

                List<DataElement> dataElementList = dataElementMap.get(dataContainerList.get(0).getDbName());

                // 手动构建数据容器需要的JSON
                List<String> appendList = new ArrayList<>();
                for (DataElement item : dataElementList) {
                    appendList.add("\"" + FieldUtil.underlineToCamel(item.getFieldName()) + "\":"); // 格式 "testAddtime":
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("[");
                for (int x = 0; x < results.length; x++) {
                    stringBuilder.append("{");
                    for (int y = 0; y < results[x].length; y++) {
                        stringBuilder.append(appendList.get(y) + "\"" + results[x][y] + "\"");
                        if (y < results[x].length -1) {
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.append("}");
                    if (x < results.length -1) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append("]");

                // 获取dataKey
                String itemDetailJson = HttpClientUtil.getInstance().sendHttpGet(5000,
                        dataManageAddress + "/catalog/item/detail/" + itemId);
                DataItem itemDetail = JSON.parseObject(itemDetailJson,
                        new TypeReference<DataItem>() {});

                System.out.println(JSON.toJSONString(itemDetail));
                String dataKey = itemDetail.getDataKey();

                System.out.println("dataKey: " + dataKey);
                System.out.println("stringBuilder: " + stringBuilder.toString());

                String topic = null;
                try {
                    topic = crudAccessService.batchCreate(dataKey, stringBuilder.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("topic: " + topic);

                DataAccessBackResult dataAccessBackResult = JSON.parseObject(topic, DataAccessBackResult.class);
                System.out.println("===========");
                System.out.println(JSON.toJSONString(dataAccessBackResult));
                System.out.println("===========");
                return "0";
            } else {
                return "1"; // exit code: 0=success, 1=error
            }
        }
    }


    private String saveLogToDatabase(int id, String title, String queryId, int save2db, Long itemId) {
        PreparedStatement statement = null;
        // 根据queryId获取运行结果
        String resultUrl = prestoAPI + "/history?datasource=" + datasource + "&queryid=" + queryId;
        String resultJson = HttpClientUtil.getInstance().sendHttpGet(5000, resultUrl);
        PrestoResults result = JSON.parseObject(resultJson, new TypeReference<PrestoResults>() {
        });

        System.out.println("save2db: " + save2db);
        if (save2db == 1) { // 如果需要定时存储查询结果到数据库
            batchCreateCallable batchCreateCallable = new batchCreateCallable(resultJson, itemId, dataManageAddress);
            System.out.println("---------------------------------");
            System.out.println("resultJson: " + resultJson);
            System.out.println("itemId: " + itemId);
            System.out.println("dataManageAddress: " + dataManageAddress);
            System.out.println("---------------------------------");
            FutureTask<String> futureTask = new FutureTask<String>(batchCreateCallable);
            new Thread(futureTask).start();
        }

        // 运行后需要把运行日志加入到数据库log_info表中
        result.setQueryid(queryId);
        try {
            if (result.getError() == null) { // 运行结果正确
                statement = con.prepareStatement("INSERT INTO log_info(job_id, query_id, query_name, finished_time, state, trigger_msg, query_string, result) VALUES (?,?,?,?,?,?,?,?)");
                // 修改时间格式
                String originFinishedTime = result.getFinishedTime();
                int indexOfT = originFinishedTime.indexOf("T");
                int indexOfPlus = originFinishedTime.indexOf("+");
                String finishedTime = originFinishedTime.substring(0, indexOfT) + " " + originFinishedTime.substring(indexOfT + 1, indexOfPlus - 4);
                statement.setInt(1, id);
                statement.setString(2, queryId);
                statement.setString(3, title);
                statement.setString(4, finishedTime);
                statement.setString(5, "Finished");
                statement.setString(6, "Success");
                statement.setString(7, result.getQueryString());
                statement.setString(8, csvDownload(queryId));
                statement.execute();
            } else { // 运行结果错误
                statement = con.prepareStatement("INSERT INTO log_info(job_id, query_id, query_name, finished_time, state, trigger_msg, query_string, result) VALUES (?,?,?,?,?,?,?,?)");
                // 修改时间格式
                String originFinishedTime = result.getFinishedTime();
                int indexOfT = originFinishedTime.indexOf("T");
                int indexOfPlus = originFinishedTime.indexOf("+");
                String finishedTime = originFinishedTime.substring(0, indexOfT) + " " + originFinishedTime.substring(indexOfT + 1, indexOfPlus - 4);
                statement.setInt(1, id);
                statement.setString(2, queryId);
                statement.setString(3, title);
                statement.setString(4, finishedTime);
                statement.setString(5, "Error");
                statement.setString(6, result.getError());
                statement.setString(7, result.getQueryString());
                statement.setString(8, csvDownload(queryId));
                statement.execute();
            }
            return "0"; // exit code: 0=success, 1=error
        } catch (SQLException e) {
            e.printStackTrace();
            return "1";
        } finally {
            closeStatementAndResultSet(statement, null);
        }
    }

    /**
     * 下载sql运行结果的csv文件
     */
    private String csvDownload(String queryId) {
        String url = prestoAPI + "/csvdownload";
        String param = "datasource=" + datasource + "&queryid=" + queryId + "&encode=UTF-8";

        return url + "?" + param;
    }

    /**
     * @param statement
     * @param resultSet
     * @Description 关闭statement，resultSet
     * @date: 2018年8月8日 下午3:36:30
     */
    private static void closeStatementAndResultSet(Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}