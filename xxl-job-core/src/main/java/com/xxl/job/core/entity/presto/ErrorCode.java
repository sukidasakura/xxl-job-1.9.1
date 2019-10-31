package com.xxl.job.core.entity.presto;

import java.io.Serializable;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年08月07日 11:17
 */
public class ErrorCode implements Serializable {

    private static final long serialVersionUID = 2554141901018700150L;

    private long code;

    private String name;

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
