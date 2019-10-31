package com.xxl.job.core.entity.presto.catalog;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月25日 15:03
 */
public class DataContainer implements Serializable {
    /**
     * @Fields serialVersionUID:
     */
    private static final long serialVersionUID = 1900383139958760066L;

    private long id;

    private int priorityContid;

    /** 中文名称 **/
    private String cnName;

    /** 容器名称 **/
    private String containerName;

    /** 容器类型 **/
    private int containerType;

    /** 容器类型名称 **/
    private String containerTypeName;

    /** IP **/
    private String ip;

    /** 端口 **/
    private int port;

    /** 用户名 **/
    private String userName;

    /** 密码 **/
    private String password;

    /** 最大连接数 **/
    private int maxActive;

    /** 如Oracle-orcl,elasticsearch-cluster.name **/
    private String dbName;

    /** 描述 **/
    private String description;

    /** 录入时间 **/
    private Date createTime;

    /** 更新时间 **/
    private Date updateTime;
    /**
     * 数据容器状态
     */
    private int status;

    /** 表空间 */
    private String tableSpace;

    /** 行业类型 */
    private String industry;

    /** 序号 **/
    private int seqNum;

    private int databaseId;

    /** 创建者 **/
    private String owner;

    private String createTimeStr;

    private String updateTimeStr;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPriorityContid() {
        return priorityContid;
    }

    public void setPriorityContid(int priorityContid) {
        this.priorityContid = priorityContid;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public int getContainerType() {
        return containerType;
    }

    public void setContainerType(int containerType) {
        this.containerType = containerType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContainerTypeName() {
        return containerTypeName;
    }

    public void setContainerTypeName(String containerTypeName) {
        this.containerTypeName = containerTypeName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getTableSpace() {
        return tableSpace;
    }

    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }



    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }


    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }

    public String getUpdateTimeStr() {
        return updateTimeStr;
    }

    public void setUpdateTimeStr(String updateTimeStr) {
        this.updateTimeStr = updateTimeStr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }
        if (obj instanceof DataContainer) {
            DataContainer container = (DataContainer) obj;
            if (container.containerName.equals(this.containerName) && container.id == this.id) {
                return true;
            }
        }
        return false;
    }


    /**
     * Description
     *
     * @return
     * @see java.lang.Object#hashCode()
     */

    @Override
    public int hashCode() {

        String name = containerName;
        return name.hashCode();
    }

}
