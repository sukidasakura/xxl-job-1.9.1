package com.xxl.job.core.entity.presto;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年08月07日 13:46
 */
public class FailureInfo implements Serializable{

    private static final long serialVersionUID = 1897399724216441557L;

    private String message;

    private List<String> stack;

    private String type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getStack() {
        return stack;
    }

    public void setStack(List<String> stack) {
        this.stack = stack;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
