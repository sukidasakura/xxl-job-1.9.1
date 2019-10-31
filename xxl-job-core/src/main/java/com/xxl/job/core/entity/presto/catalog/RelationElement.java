package com.xxl.job.core.entity.presto.catalog;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月25日 15:11
 */
public class RelationElement implements Serializable {
    /**
     * @Fields serialVersionUID:
     */
    private static final long serialVersionUID = -3908032599861451566L;

    private long id;

    private long relationItemId;

    private long itemColumnId;

    private long relationColumnId;

    private String relationColumn;

    private String itemColumn;

    private String relationColumnCnName;

    private String itemColumnCnName;

    @SuppressWarnings("rawtypes")
    private HashMap columnMap;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRelationItemId() {
        return relationItemId;
    }

    @SuppressWarnings("rawtypes")
    public HashMap getColumnMap() {
        return columnMap;
    }

    @SuppressWarnings("rawtypes")
    public void setColumnMap(HashMap columnMap) {
        this.columnMap = columnMap;
    }

    public void setRelationItemId(long relationItemId) {
        this.relationItemId = relationItemId;
    }

    public String getRelationColumn() {
        return relationColumn;
    }

    public void setRelationColumn(String relationColumn) {
        this.relationColumn = relationColumn;
    }

    public String getItemColumn() {
        return itemColumn;
    }

    public void setItemColumn(String itemColumn) {
        this.itemColumn = itemColumn;
    }

    public long getItemColumnId() {
        return itemColumnId;
    }

    public void setItemColumnId(long itemColumnId) {
        this.itemColumnId = itemColumnId;
    }

    public long getRelationColumnId() {
        return relationColumnId;
    }

    public void setRelationColumnId(long relationColumnId) {
        this.relationColumnId = relationColumnId;
    }

    public String getRelationColumnCnName() {
        return relationColumnCnName;
    }

    public void setRelationColumnCnName(String relationColumnCnName) {
        this.relationColumnCnName = relationColumnCnName;
    }

    public String getItemColumnCnName() {
        return itemColumnCnName;
    }

    public void setItemColumnCnName(String itemColumnCnName) {
        this.itemColumnCnName = itemColumnCnName;
    }
}
