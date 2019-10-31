package com.xxl.job.core.entity.presto;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月10日 15:32
 */
public class QueryStatus {
    private static final long serialVersionUID = 6234108344740789399L;

    private boolean clearTransactionId;

    private ErrorCode errorCode;

    private String errorType;

    private FailureInfo failureInfo;

    private String memoryPool;

    private String query;

    private String queryId;

    private boolean scheduled;

    // PLANNING / FAILED / FINISHED
    private String state;

    public boolean isClearTransactionId() {
        return clearTransactionId;
    }

    public void setClearTransactionId(boolean clearTransactionId) {
        this.clearTransactionId = clearTransactionId;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public FailureInfo getFailureInfo() {
        return failureInfo;
    }

    public void setFailureInfo(FailureInfo failureInfo) {
        this.failureInfo = failureInfo;
    }

    public String getMemoryPool() {
        return memoryPool;
    }

    public void setMemoryPool(String memoryPool) {
        this.memoryPool = memoryPool;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
