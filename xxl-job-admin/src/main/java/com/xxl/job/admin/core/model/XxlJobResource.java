package com.xxl.job.admin.core.model;

import java.io.Serializable;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2018年09月03日 16:54
 */
public class XxlJobResource implements Serializable {

    private static final long serialVersionUID = 1465974389999536121L;

    /** 主键ID **/
    private int id;

    /** 资源名 **/
    private String fileName;

    /** 资源描述 **/
    private String describe;

    /** 添加时间 **/
    private String addTime;

    private String type;

    /** 文件内容, 二进制字段**/
    private byte[] content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getAddTime() {
        return addTime;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
