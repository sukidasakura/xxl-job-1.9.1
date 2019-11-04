package com.xxl.job.core.entity.presto;

import java.io.Serializable;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月15日 18:03
 */
public class PrestoParam implements Serializable{

    private String yanagishimaAddress;
    private String dataSource;
    private String prestoDbJdbcUrl;
    private String prestoDbUsername;
    private String prestoDbPassword;
    private String prestoDbDriver;
    private int jobId;
    private String title;
    private String query;
    private int save2db; // 是否将查询结果定时存储到数据库
    private Long itemId; // 数据容器接口需要的ID
    private String dataManageAddress;
    private String orderedElements; // 需要存储到数据库的数据元(字段)有序列表

    public String getDataManageAddress() {
        return dataManageAddress;
    }

    public void setDataManageAddress(String dataManageAddress) {
        this.dataManageAddress = dataManageAddress;
    }

    public String getYanagishimaAddress() {
        return yanagishimaAddress;
    }

    public void setYanagishimaAddress(String yanagishimaAddress) {
        this.yanagishimaAddress = yanagishimaAddress;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getPrestoDbJdbcUrl() {
        return prestoDbJdbcUrl;
    }

    public void setPrestoDbJdbcUrl(String prestoDbJdbcUrl) {
        this.prestoDbJdbcUrl = prestoDbJdbcUrl;
    }

    public String getPrestoDbUsername() {
        return prestoDbUsername;
    }

    public void setPrestoDbUsername(String prestoDbUsername) {
        this.prestoDbUsername = prestoDbUsername;
    }

    public String getPrestoDbPassword() {
        return prestoDbPassword;
    }

    public void setPrestoDbPassword(String prestoDbPassword) {
        this.prestoDbPassword = prestoDbPassword;
    }

    public String getPrestoDbDriver() {
        return prestoDbDriver;
    }

    public void setPrestoDbDriver(String prestoDbDriver) {
        this.prestoDbDriver = prestoDbDriver;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getSave2db() {
        return save2db;
    }

    public void setSave2db(int save2db) {
        this.save2db = save2db;
    }

    public String getOrderedElements() {
        return orderedElements;
    }

    public void setOrderedElements(String orderedElements) {
        this.orderedElements = orderedElements;
    }
}
