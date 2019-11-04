package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.core.biz.model.ReturnT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 执行器
 * job group controller
 *
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

    @Resource
    public XxlJobInfoDao xxlJobInfoDao;
    @Resource
    public XxlJobGroupDao xxlJobGroupDao;

    private static ReentrantLock lock = new ReentrantLock();

    @RequestMapping
    public String index(Model model) {

        // job group (executor)
        List<XxlJobGroup> list = xxlJobGroupDao.findAll();

        model.addAttribute("list", list);
        return "jobgroup/jobgroup.index";
    }

    /**
     * 获取所有执行器
     *
     * @return
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    @ResponseBody
    public List<XxlJobGroup> findAll() {
        return xxlJobGroupDao.findAll();
    }

    /**
     * 根据分页返回执行器（用于data_Center）
     *
     * @param start
     * @param length
     * @return
     */
    @RequestMapping(value = "/findAllByPage", method = RequestMethod.POST)
    @ResponseBody
    public List<XxlJobGroup> findAllByPage(@RequestParam(required = false, defaultValue = "0") int start,
                                           @RequestParam(required = false, defaultValue = "10") int length) {
        return xxlJobGroupDao.findAllByPage(start, length);
    }


    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String appName,
                                        int addressType) {
        // page list
        List<XxlJobGroup> list = xxlJobGroupDao.pageList(start, length, appName, addressType);
        int list_count = xxlJobGroupDao.pageListCount(start, length, appName, addressType);

        // package result
        Map<String, Object> maps = new HashMap<>();
        maps.put("recordsTotal", list_count);       // 总记录数
        maps.put("recordsFiltered", list_count);  // 过滤后的总记录数
        maps.put("data", list);     // 分页列表

        return maps;
    }


    /**
     * 获取执行器的数量
     *
     * @return
     */
    @RequestMapping(value = "counts", method = RequestMethod.GET)
    @ResponseBody
    public long counts() {
        return xxlJobGroupDao.counts();
    }


    /**
     * 新增后，保存执行器(用于data_center)
     *
     * @param xxlJobGroup
     * @return
     */
    @RequestMapping("/restSave")
    @ResponseBody
    public ReturnT<String> restSave(@RequestBody XxlJobGroup xxlJobGroup) {

        // valid
        if (xxlJobGroup.getAppName() == null || StringUtils.isBlank(xxlJobGroup.getAppName())) {
            return new ReturnT<String>(500, ("请输入名称"));
        }
        if (xxlJobGroup.getAppName().length() < 4 || xxlJobGroup.getAppName().length() > 64) { // AppName长度限制为4~64
            return new ReturnT<String>(500, "执行器名称长度限制为4~64");
        }
        if (xxlJobGroup.getTitle() == null || StringUtils.isBlank(xxlJobGroup.getTitle())) {
            return new ReturnT<String>(500, "请输入描述");
        }
        xxlJobGroup.setAddressType(1); // 设置注册方式为手动录入
        if (StringUtils.isBlank(xxlJobGroup.getAddressList())) {
            return new ReturnT<String>(500, "手动录入注册方式，机器地址不可为空");
        }
        String[] addresss = xxlJobGroup.getAddressList().split(",");
        for (String item : addresss) {
            if (StringUtils.isBlank(item)) {
                return new ReturnT<String>(500, "机器地址格式非法");
            }
        }

        int ret = 0;
        try {
            lock.lock();
            int maxId = xxlJobGroupDao.findMaxId();
            if (maxId > 20) maxId ++;
            else maxId = 21;
            xxlJobGroup.setId(maxId);
            ret = xxlJobGroupDao.save(xxlJobGroup);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }


    // 根据id加载执行器
    @RequestMapping(value = "/load", method = RequestMethod.POST)
    @ResponseBody
    public XxlJobGroup load(int id) {
        return xxlJobGroupDao.loadById(id);
    }


    /**
     * 新增后，保存执行器(用于xxl-job本身)
     *
     * @param xxlJobGroup
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public ReturnT<String> save(XxlJobGroup xxlJobGroup) {

        // valid
        if (xxlJobGroup.getAppName() == null || StringUtils.isBlank(xxlJobGroup.getAppName())) {
            return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + "AppName"));
        }
        if (xxlJobGroup.getAppName().length() < 4 || xxlJobGroup.getAppName().length() > 64) { // AppName长度限制为4~64
            return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appName_length"));
        }
        if (xxlJobGroup.getTitle() == null || StringUtils.isBlank(xxlJobGroup.getTitle())) {
            return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")));
        }
        if (xxlJobGroup.getAddressType() != 0) { // 设置注册方式为手动录入
            if (StringUtils.isBlank(xxlJobGroup.getAddressList())) {
                return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit"));
            }
            String[] addresss = xxlJobGroup.getAddressList().split(",");
            for (String item : addresss) {
                if (StringUtils.isBlank(item)) {
                    return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid"));
                }
            }
        }

        int ret = 0;
        try {
            lock.lock();
            int id = xxlJobGroupDao.findMaxId();
            xxlJobGroup.setId(id + 1);
            ret = xxlJobGroupDao.save(xxlJobGroup);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }


    /**
     * 更新执行器
     *
     * @param xxlJobGroup
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public ReturnT<String> update(XxlJobGroup xxlJobGroup) {
        // valid
        if (xxlJobGroup.getAppName() == null || StringUtils.isBlank(xxlJobGroup.getAppName())) {
            return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + "AppName"));
        }
        if (xxlJobGroup.getAppName().length() < 4 || xxlJobGroup.getAppName().length() > 64) {
            return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appName_length"));
        }
        if (xxlJobGroup.getTitle() == null || StringUtils.isBlank(xxlJobGroup.getTitle())) {
            return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")));
        }
        if (xxlJobGroup.getAddressType() != 0) {
            if (StringUtils.isBlank(xxlJobGroup.getAddressList())) { //手动录入注册方式，机器地址不可为空
                return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit"));
            }
            String[] addresss = xxlJobGroup.getAddressList().split(",");
            for (String item : addresss) {
                if (StringUtils.isBlank(item)) {
                    return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid"));
                }
            }
        }

        int ret = xxlJobGroupDao.update(xxlJobGroup);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    /**
     * 更新执行器(用于data_center)
     *
     * @param xxlJobGroup
     * @return
     */
    @RequestMapping("/restUpdate")
    @ResponseBody
    public ReturnT<String> restUpdate(@RequestBody XxlJobGroup xxlJobGroup) {
        // valid
        if (xxlJobGroup.getAppName() == null || StringUtils.isBlank(xxlJobGroup.getAppName())) {
            return new ReturnT<String>(500, "请输入执行器名称");
        }
        if (xxlJobGroup.getAppName().length() < 4 || xxlJobGroup.getAppName().length() > 64) {
            return new ReturnT<String>(500, "执行器名称长度限制为4~64");
        }
        if (xxlJobGroup.getTitle() == null || StringUtils.isBlank(xxlJobGroup.getTitle())) {
            return new ReturnT<String>(500, "请输入描述");
        }
        // 默认为手动注册方式
        xxlJobGroup.setAddressType(1);
        if (StringUtils.isBlank(xxlJobGroup.getAddressList())) { //手动录入注册方式，机器地址不可为空
            return new ReturnT<String>(500, "手动录入注册方式，机器地址不可为空");
        }
        String[] addresss = xxlJobGroup.getAddressList().split(",");
        for (String item : addresss) {
            if (StringUtils.isBlank(item)) {
                return new ReturnT<String>(500, "机器地址格式非法");
            }

        }

        int ret = xxlJobGroupDao.update(xxlJobGroup);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    /**
     * 移除执行器
     *
     * @param id
     * @return
     */
    @RequestMapping("/remove")
    @ResponseBody
    public ReturnT<String> remove(int id) {

        // valid
        int count = xxlJobInfoDao.pageListCount(0, 10, id, null, null);
        if (count > 0) {
            return new ReturnT<String>(500, I18nUtil.getString("jobgroup_del_limit_0")); //该执行器使用中
        }

        List<XxlJobGroup> allList = xxlJobGroupDao.findAll();
        if (allList.size() == 1) {
            return new ReturnT<String>(500, I18nUtil.getString("jobgroup_del_limit_1")); // 拒绝删除, 系统至少保留一个执行器
        }

        int ret = xxlJobGroupDao.remove(id);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

}
