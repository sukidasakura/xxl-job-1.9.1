package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlJobResource;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobResourceDao;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.ResourceTypeEnum;
import com.xxl.job.core.util.DateTool;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 资源管理
 * @author: mashencai@supcon.com
 * @date: 2018年10月09日 9:46
 */
@RestController
@RequestMapping("/jobResource")
public class JobResourceController {

    @Resource
    public XxlJobResourceDao xxlJobResourceDao;

    @Resource
    public XxlJobInfoDao xxlJobInfoDao;

    /**
     * 列出所有资源
     * @param
     * @return
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.POST)
    public List<XxlJobResource> findAll(@RequestParam(required = false, defaultValue = "0")int start,
                                        @RequestParam(required = false, defaultValue = "10")int length){
        return xxlJobResourceDao.findAll(start, length);
    }

    /**
     * 获取资源的数量
     * @return
     */
    @RequestMapping(value = "counts", method = RequestMethod.GET)
    @ResponseBody
    public long counts(){
        return xxlJobResourceDao.counts();
    }


    /**
     * 根据资源名称、资源描述、资源类型、每页记录数、第几页来查找符合条件的资源列表
     * @param start
     * @param length
     * @param fileName
     * @param describe
     * @param type
     * @return
     */
    @RequestMapping("/pageList")
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0")int start,
                                        @RequestParam(required = false, defaultValue = "10")int length,
                                        String fileName,
                                        String describe,
                                        String type){
        // page list
        List<XxlJobResource> list = xxlJobResourceDao.pageList(start, length, fileName, describe, type);
        int list_count = xxlJobResourceDao.pageListCount(start, length, fileName, describe, type);

        // package result
        Map<String, Object> maps = new HashMap<>();
        maps.put("recordsTotal", list_count);       // 总记录数
        maps.put("recordsFiltered", list_count);  // 过滤后的总记录数
        maps.put("data", list);     // 分页列表

        return maps;
    }

    /**
     * 新增上传资源
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> uploadResource(@RequestBody XxlJobResource xxlJobResource) throws IOException {
        ReturnT returnT = new ReturnT();

        if (xxlJobResourceDao.fileNameExist(xxlJobResource.getFileName()) != 0){
            returnT.setCode(500);
            returnT.setMsg("文件已存在, 重新选择文件");
            return returnT;
        }

        int result = xxlJobResourceDao.upload(xxlJobResource);
        if (result > 0){
            int resourceId = xxlJobResourceDao.getIdByFileName(xxlJobResource.getFileName()).getId();
            returnT.setCode(ReturnT.SUCCESS_CODE);
            returnT.setMsg("新增成功，返回resourceId");
            returnT.setContent(resourceId);
        } else {
            returnT.setCode(ReturnT.FAIL_CODE);
            returnT.setMsg("新增失败");
        }
        return returnT;

    }

    /**
     * 删除资源, 尚未考虑资源是否在被使用
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> delete(@ModelAttribute("id")int id){

        ReturnT returnT = new ReturnT();

        List<Integer> usedResources = xxlJobInfoDao.getUsedResources();
        for (int item : usedResources){
            if (item == id) { // 如果资源是被使用中的话, 不允许删除
                returnT.setCode(500);
                returnT.setMsg("资源使用中, 不允许删除");
                return returnT;
            }
        }

        int result = xxlJobResourceDao.delete(id);
        return (result > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    /**
     * 删除资源, 尚未考虑资源是否在被使用
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/deleteByName", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> deleteByName(@ModelAttribute("fileName")String fileName){

        ReturnT returnT = new ReturnT();

        int resourceId = xxlJobResourceDao.getIdByFileName(fileName).getId();
        List<Integer> usedResources = xxlJobInfoDao.getUsedResources();
        for (int item : usedResources){
            if (item == resourceId) { // 如果资源是被使用中的话, 不允许删除
                returnT.setCode(500);
                returnT.setMsg("资源使用中, 不允许删除");
                return returnT;
            }
        }

        int result = xxlJobResourceDao.delete(resourceId);
        return (result > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }


}
