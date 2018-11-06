package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2018年10月08日 10:38
 */
public interface XxlJobResourceDao {
    /**
     * 根据资源名称、资源描述、资源类型、每页记录数、第几页来查找符合条件的资源列表
     * @param offset 第几页开始
     * @param pagesize 一页中放几条记录
     * @param fileName
     * @param describe
     * @param type
     * @return
     */
    List<XxlJobResource> pageList(@Param("offset") int offset,
                                  @Param("pagesize") int pagesize,
                                  @Param("fileName") String fileName,
                                  @Param("describe") String describe,
                                  @Param("type") String type);

    int pageListCount(@Param("offset") int offset,
                      @Param("pagesize") int pagesize,
                      @Param("fileName") String fileName,
                      @Param("describe") String describe,
                      @Param("type") String type);

    // 查找记录数
    long counts();

    // 查找所有资源
    List<XxlJobResource> findAll(@Param("offset") int offset,
                                 @Param("pagesize") int pagesize);

    // 上传文件资源到数据库
    int upload(XxlJobResource resource);

    // 根据Id删除资源
    int delete(@Param("id") int id);

    // 根据资源id加载资源信息
    XxlJobResource loadById(@Param("id") int id);

}
