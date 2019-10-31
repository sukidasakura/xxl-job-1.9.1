package com.xxl.job.core.entity.presto.catalog;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2019年10月25日 15:10
 */
public class RelationItem  implements Serializable {
    /**
     * @Fields serialVersionUID:
     */
    private static final long serialVersionUID = 4461154070806966835L;

    private long id;

    private long dataItemId;

    private long relationItemId;

    private String dataItemName;

    private String relationItem;

    private long mainItemId;

    private int relationType;

    /** 关联数据元 **/
    private List<RelationElement> relationElements;

    private String relationElementJson;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDataItemId() {
        return dataItemId;
    }

    public void setDataItemId(long dataItemId) {
        this.dataItemId = dataItemId;
    }

    public String getRelationItem() {
        return relationItem;
    }

    public void setRelationItem(String relationItem) {
        this.relationItem = relationItem;
    }

    public int getRelationType() {
        return relationType;
    }

    public void setRelationType(int relationType) {
        this.relationType = relationType;
    }

    public List<RelationElement> getRelationElements() {
        return relationElements;
    }

    public void setRelationElements(List<RelationElement> relationElements) {
        this.relationElements = relationElements;
    }

    public String getDataItemName() {
        return dataItemName;
    }

    public void setDataItemName(String dataItemName) {
        this.dataItemName = dataItemName;
    }

    public long getRelationItemId() {
        return relationItemId;
    }

    public void setRelationItemId(long relationItemId) {
        this.relationItemId = relationItemId;
    }

    public String getRelationElementJson() {
        return relationElementJson;
    }

    public void setRelationElementJson(String relationElementJson) {
        this.relationElementJson = relationElementJson;
    }

    public long getMainItemId() {
        return mainItemId;
    }

    public void setMainItemId(long mainItemId) {
        this.mainItemId = mainItemId;
    }

}
