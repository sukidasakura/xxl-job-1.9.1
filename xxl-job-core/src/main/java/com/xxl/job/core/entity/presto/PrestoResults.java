package com.xxl.job.core.entity.presto;

import java.io.Serializable;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年01月15日 9:53
 */
public class PrestoResults implements Serializable{

    private static final long serialVersionUID = 695633278780866733L;

    private String[] headers;

    private String rawDataSize;

    private String finishedTime;

    private String queryString;

    private String lineNumber;

    private String[][] results;

    private int errorLineNumber;

    private String error;

    private String queryid;

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public String getRawDataSize() {
        return rawDataSize;
    }

    public void setRawDataSize(String rawDataSize) {
        this.rawDataSize = rawDataSize;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String[][] getResults() {
        return results;
    }

    public void setResults(String[][] results) {
        this.results = results;
    }

    public String getQueryid() {
        return queryid;
    }

    public void setQueryid(String queryid) {
        this.queryid = queryid;
    }

    public String getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(String finishedTime) {
        this.finishedTime = finishedTime;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public int getErrorLineNumber() {
        return errorLineNumber;
    }

    public void setErrorLineNumber(int errorLineNumber) {
        this.errorLineNumber = errorLineNumber;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
