package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlJobResource;
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

//    /**
//     * 新增上传资源
//     * @param request
//     * @param response
//     * @param file 文件
//     * @param describe 文件描述
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(value = "/upload", method = RequestMethod.POST)
//    @ResponseBody
//    public ReturnT<String> uploadResource(HttpServletRequest request,
//                                          HttpServletResponse response,
//                                          @RequestParam(value = "file")MultipartFile file,
//                                          @RequestParam(value = "describe", required = false)String describe) throws IOException {
//
//        // 上传的文件名file.jar
//        String originName = file.getOriginalFilename();
//        // 后缀名
//        String _suffix = originName.substring(originName.lastIndexOf(".") + 1, originName.length());
//        // 支持类型：jar, py, txt, sh, sql
//        ResourceTypeEnum resourceType = ResourceTypeEnum.match(_suffix.toUpperCase(), null);
//        if (resourceType == null){
//            return new ReturnT<>(500, "上传文件格式有误");
//        }
//        XxlJobResource xxlJobResource = new XxlJobResource();
//        byte[] bytes = file.getBytes();
//        xxlJobResource.setFileName(originName); // 文件名
//        xxlJobResource.setDescribe(describe); // 文件描述
//        xxlJobResource.setContent(bytes); // 文件内容
//        xxlJobResource.setAddTime(DateTool.convertDateTime(new Date())); // 添加时间
//        xxlJobResource.setType(resourceType.getSuffix());
//
//        int result = xxlJobResourceDao.upload(xxlJobResource);
//        return (result > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
//
//    }

    /**
     * 新增上传资源
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> uploadResource(@RequestBody XxlJobResource xxlJobResource) throws IOException {

        int result = xxlJobResourceDao.upload(xxlJobResource);
        return (result > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;

    }

    /**
     * 删除资源, 尚未考虑资源是否在被使用
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> delete(@ModelAttribute("id")int id){
        int result = xxlJobResourceDao.delete(id);
        return (result > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }
}
