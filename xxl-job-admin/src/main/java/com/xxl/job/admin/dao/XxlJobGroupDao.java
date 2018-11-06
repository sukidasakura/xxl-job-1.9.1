package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by xuxueli on 16/9/30.
 */
public interface XxlJobGroupDao {

    // 查找记录数
    long counts();

    // 查找所有执行器，按照排序升序显示
    // 排序：执行器的排序, 系统中需要执行器的地方,如任务新增, 将会按照该排序读取可用的执行器列表;
    List<XxlJobGroup> findAll();

    List<XxlJobGroup> findAllByPage(@Param("offset") int offset,
                              @Param("pagesize") int pagesize);

    List<XxlJobGroup> pageList(@Param("offset") int offset,
                                  @Param("pagesize") int pagesize,
                                  @Param("appName") String appName,
                                  @Param("addressType") int addressType);

    int pageListCount(@Param("offset") int offset,
                      @Param("pagesize") int pagesize,
                      @Param("appName") String appName,
                      @Param("addressType") int addressType);

    // 根据执行器地址类型(自动注册、手动注册)查找执行器
    List<XxlJobGroup> findByAddressType(@Param("addressType") int addressType);

    // 根据任务id加载任务信息
    XxlJobGroup loadById(@Param("id") int id);

    // 保存新增的执行器信息
    int save(XxlJobGroup xxlJobGroup);

    // 更新修改的执行器信息
    int update(XxlJobGroup xxlJobGroup);

    // 根据执行器Id删除执行器信息
    int remove(@Param("id") int id);

    // 根据执行器Id加载执行器信息
    XxlJobGroup load(@Param("id") int id);

}
