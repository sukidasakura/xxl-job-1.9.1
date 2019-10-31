package com.xxl.job.core.entity.presto.catalog;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月25日 15:10
 */
public class DataItem implements Serializable {
    /**
     * @Fields serialVersionUID:
     */

    private static final long serialVersionUID = 7230095534345319733L;

    private long id;

    /** 数据容器 **/
    private long containerId;

    /** 中文名称 **/
    private String cnName;

    /** 数据项名称(表名) **/
    private String itemName;

    /** 数据来源 (待定) **/
    private String dataSource;

    private String dataSourceName;

    /** 数据类型标识符 **/
    private String dataKey;

    /** 描述 **/
    private String description;

    /** 数据类型(静态数据、动态数据....) **/
    private int itemType;

    /** 是否基础数据项 **/
    private boolean isBaseItem;

    /** 业务大类 **/
    private String businessType;

    private String businessTypeName;

    /** 业务二类 **/
    private String businessSecondType;

    /** 关联数据项 **/
    private List<RelationItem> relationItems;

    /** 录入时间 **/
    private Date createTime;

    /** 更新时间 **/
    private Date updateTime;

    /** 数据项存储位置列表 **/
    private List<DataContainer> containers;

    /** 数据项对应数据库中的条数 */
    private long itemTotalNum;

    /** 数据元的数量 */
    private int elementsNum;

    /** 关联数据元集合 **/
    private List<DataElement> dataElements;

    /** 行业分类 */
    private String industry;

    /** 是否被引用 */
    private boolean isQuote;

    /** 数据元集合JSON格式 */
    private String elementsJson;

    /** 数据容器集合JSON格式 */
    private String containersJson;

    /** 关联数据项集合JSON格式 */
    private String relationItemJson;

    /** 索引前缀 */
    private String indexPrefix;

    /** 序号 **/
    private int seqNum;

    /** 发布状态 **/
    private int publishStatus;

    /** 创建用户 **/
    private String owner;

    private String createTimeStr;

    private String updateTimeStr;

    private String templetItemName;

    private List<DataElement> addElements;

    private List<DataElement> deleteElements;

    private DataElement deleteElement;

    private DataElement updateElement;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContainerId() {
        return containerId;
    }

    public void setContainerId(long containerId) {
        this.containerId = containerId;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public boolean isBaseItem() {
        return isBaseItem;
    }

    public void setBaseItem(boolean isBaseItem) {
        this.isBaseItem = isBaseItem;
    }

    public List<RelationItem> getRelationItems() {
        return relationItems;
    }

    public void setRelationItems(List<RelationItem> relationItems) {
        this.relationItems = relationItems;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessSecondType() {
        return businessSecondType;
    }

    public void setBusinessSecondType(String businessSecondType) {
        this.businessSecondType = businessSecondType;
    }

    public List<DataContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<DataContainer> containers) {
        this.containers = containers;
    }

    public Long getItemTotalNum() {
        return itemTotalNum;
    }

    public void setItemTotalNum(Long itemTotalNum) {
        this.itemTotalNum = itemTotalNum;
    }

    public int getElementsNum() {
        return elementsNum;
    }

    public void setElementsNum(int elementsNum) {
        this.elementsNum = elementsNum;
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

    public List<DataElement> getDataElements() {
        return dataElements;
    }

    public void setDataElements(List<DataElement> dataElements) {
        this.dataElements = dataElements;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getElementsJson() {
        return elementsJson;
    }

    public void setElementsJson(String elementsJson) {
        this.elementsJson = elementsJson;
    }

    public String getContainersJson() {
        return containersJson;
    }

    public void setContainersJson(String containersJson) {
        this.containersJson = containersJson;
    }

    public boolean isQuote() {
        return isQuote;
    }

    public void setQuote(boolean isQuote) {
        this.isQuote = isQuote;
    }

    public String getRelationItemJson() {
        return relationItemJson;
    }

    public void setRelationItemJson(String relationItemJson) {
        this.relationItemJson = relationItemJson;
    }

    public String getIndexPrefix() {
        return indexPrefix;
    }

    public void setIndexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public void setItemTotalNum(long itemTotalNum) {
        this.itemTotalNum = itemTotalNum;
    }

    public int getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(int publishStatus) {
        this.publishStatus = publishStatus;
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

    public String getTempletItemName() {
        return templetItemName;
    }

    public void setTempletItemName(String templetItemName) {
        this.templetItemName = templetItemName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getBusinessTypeName() {
        return businessTypeName;
    }

    public void setBusinessTypeName(String businessTypeName) {
        this.businessTypeName = businessTypeName;
    }

    public List<DataElement> getAddElements() {
        return addElements;
    }

    public void setAddElements(List<DataElement> addElements) {
        this.addElements = addElements;
    }

    public List<DataElement> getDeleteElements() {
        return deleteElements;
    }

    public void setDeleteElements(List<DataElement> deleteElements) {
        this.deleteElements = deleteElements;
    }

    public DataElement getDeleteElement() {
        return deleteElement;
    }

    public void setDeleteElement(DataElement deleteElement) {
        this.deleteElement = deleteElement;
    }

    public DataElement getUpdateElement() {
        return updateElement;
    }

    public void setUpdateElement(DataElement updateElement) {
        this.updateElement = updateElement;
    }



}
