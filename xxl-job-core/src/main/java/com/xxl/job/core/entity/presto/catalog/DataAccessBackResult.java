package com.xxl.job.core.entity.presto.catalog;

import java.io.Serializable;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月25日 15:57
 */
public class DataAccessBackResult implements Serializable {
    /** @Fields serialVersionUID: */

    private static final long serialVersionUID = -2757629853556615748L;

    /** @Fields event: 事件名称*/
    private String event;

    /** @Fields code: 事件结果：0-成功,-1失败*/
    private int code;

    /** @Fields message: 描述信息*/
    private String message;

    /** @Fields dataResult: 数据结果集*/
    private String dataResult;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDataResult() {
        return dataResult;
    }

    public void setDataResult(String dataResult) {
        this.dataResult = dataResult;
    }
}
