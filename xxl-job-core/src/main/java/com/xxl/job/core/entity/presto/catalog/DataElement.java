package com.xxl.job.core.entity.presto.catalog;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月25日 15:02
 */
public class DataElement  implements Serializable {
    /**
     * @Fields serialVersionUID:
     */
    private static final long serialVersionUID = 2138232658680107201L;

    private Long id;

    /** 数据项 **/
    private Long itemId;

    /** 中文名称 **/
    private String cnName;

    /** 数据库字段名 **/
    private String fieldName;

    /** 数据类型 **/
    private int dataType;

    /** 数据类型名称 **/
    private String dataTypeName;

    /** 是否为空 **/
    private boolean isNull;

    /** 字段长度 **/
    private int length;

    /** 默认值 **/
    private String defaultValue;

    /** 内部标识符 **/
    private String identifier;

    /** 特性词 **/
    private String traitWord;

    /** 表示词 **/
    private String expressWord;

    /** 描述 **/
    private String description;

    /** 业务大类 **/
    private String businessType;

    /** 业务二类 **/
    private String businessSecondType;

    /** 录入时间 **/
    private Date createTime;

    /** 更新时间 **/
    private Date updateTime;
    /**
     * 该数据元被引用的次数
     */
    private int quoteCount;
    /**
     * 服务中是否展示 0 展示 1不展示
     */
    private int serviceIsShow;

    /** 行业分类 **/
    private String industry;

    /** 是否被引用 **/
    private boolean isQuote;

    /** 精度 **/
    private int precision;

    /** 序号 **/
    private int seqNum;

    private String createTimeStr;

    private String updateTimeStr;

    private String owner;

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean isNull) {
        this.isNull = isNull;
    }

    public int getLength() {
        return length;
    }

    public Boolean getIsNull() {
        return isNull;
    }

    public void setIsNull(Boolean isNull) {
        this.isNull = isNull;
    }

    public void setLength(int length) {
        this.length = length;
    }


    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTraitWord() {
        return traitWord;
    }

    public void setTraitWord(String traitWord) {
        this.traitWord = traitWord;
    }

    public String getExpressWord() {
        return expressWord;
    }

    public void setExpressWord(String expressWord) {
        this.expressWord = expressWord;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public int getQuoteCount() {
        return quoteCount;
    }

    public void setQuoteCount(int quoteCount) {
        this.quoteCount = quoteCount;
    }

    public int getServiceIsShow() {
        return serviceIsShow;
    }

    public void setServiceIsShow(int serviceIsShow) {
        this.serviceIsShow = serviceIsShow;
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


    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }



    public boolean isQuote() {
        return isQuote;
    }

    public void setQuote(boolean isQuote) {
        this.isQuote = isQuote;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
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


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
    /*
     * return "DataElement [id=" + id + ", itemId=" + itemId + ", cnName=" + cnName + ", fieldName="
     * + fieldName + ", dataType=" + dataType + ", dataTypeName=" + dataTypeName + ", isNull=" +
     * isNull + ", length=" + length + ", defaultValue=" + defaultValue + ", identifier=" +
     * identifier + ", traitWord=" + traitWord + ", expressWord=" + expressWord + ", description=" +
     * description + ", businessType=" + businessType + ", businessSecondType=" + businessSecondType
     * + ", create_time=" + create_time + ", update_time=" + update_time + "]";
     */
        return JSON.toJSONString(this);

    }
}
