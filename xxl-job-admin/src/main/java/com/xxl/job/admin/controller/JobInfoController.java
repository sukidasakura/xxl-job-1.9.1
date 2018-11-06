package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.enums.ExecutorFailStrategyEnum;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {

	@Resource
	private XxlJobGroupDao xxlJobGroupDao;
	@Resource
	private XxlJobService xxlJobService;
	
	@RequestMapping
	public String index(Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

		// 枚举-字典
		model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	// 路由策略-列表
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
		model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	// 阻塞处理策略-字典
		model.addAttribute("ExecutorFailStrategyEnum", ExecutorFailStrategyEnum.values());		// 失败处理策略-字典

		// 任务组
		List<XxlJobGroup> jobGroupList =  xxlJobGroupDao.findAll();
		model.addAttribute("JobGroupList", jobGroupList);
		model.addAttribute("jobGroup", jobGroup);

		return "jobinfo/jobinfo.index";
	}

	@RequestMapping(value = "/findAll", method = RequestMethod.POST)
	@ResponseBody
	public List<XxlJobInfo> findAll(@RequestParam(required = false, defaultValue = "0") int start,
									@RequestParam(required = false, defaultValue = "10") int length){
		return xxlJobService.findAll(start, length);
	}


	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
			@RequestParam(required = false, defaultValue = "10") int length,
			int jobGroup, String jobDesc, String executorHandler, String filterTime) {
		
		return xxlJobService.pageList(start, length, jobGroup, jobDesc, executorHandler, filterTime);
	}

	@RequestMapping("/pageListByGlueType")
	@ResponseBody
	public Map<String, Object> pageListByGlueType(@RequestParam(required = false, defaultValue = "0") int start,
										@RequestParam(required = false, defaultValue = "10") int length,
										int jobGroup, String GlueType) {

		return xxlJobService.pageListByGlueType(start, length, jobGroup, GlueType);
	}



	@RequestMapping(value = "/allJobList", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String, Object>> allJobList(){
		return xxlJobService.allJobList();
	}


	/**
	 * 获取资源的数量
	 * @return
	 */
	@RequestMapping(value = "counts", method = RequestMethod.GET)
	@ResponseBody
	public long counts(){
		return xxlJobService.counts();
	}


	@RequestMapping("/restAdd")
	@ResponseBody
	public ReturnT<String> restAdd(@RequestBody XxlJobInfo jobInfo) {
		return xxlJobService.restAdd(jobInfo);
	}

	@RequestMapping("/add")
	@ResponseBody
	public ReturnT<String> add(XxlJobInfo jobInfo) {
		return xxlJobService.add(jobInfo);
	}

	// 用于data_center的restful接口
	@RequestMapping("/restUpdate")
	@ResponseBody
	public ReturnT<String> restUpdate(@RequestBody XxlJobInfo jobInfo) {
		return xxlJobService.restUpdate(jobInfo);
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(XxlJobInfo jobInfo) {
		return xxlJobService.update(jobInfo);
	}

	// 删除任务
	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id) {
		return xxlJobService.remove(id);
	}

	// 根据id加载任务
	@RequestMapping(value = "/load", method = RequestMethod.POST)
	@ResponseBody
	public XxlJobInfo load(int id){
		return xxlJobService.loadById(id);
	}

	// 手动执行一次任务(立即运行，只用于data_center)
	@RequestMapping(value = "/triggerByInfo", method = RequestMethod.POST)
	@ResponseBody
	public ReturnT<String> triggerJob(@RequestBody XxlJobInfo xxlJobInfo) {
		return xxlJobService.triggerJobByInfo(xxlJobInfo);
	}

	// 页面上点击“暂停” 按钮， 前端会发送一个请求 /jobinfo/pause  post 请求
	// param: id = 任务ID
	// controller最终会调用service的方法进行处理
	@RequestMapping("/pause")
	@ResponseBody
	public ReturnT<String> pause(int id) {
		return xxlJobService.pause(id);
	}

	// 页面上点击“恢复” 按钮， 前端会发送一个请求 /jobinfo/resume  post 请求
	// param: id = 任务ID
	// controller最终会调用service的方法进行处理
	@RequestMapping("/resume")
	@ResponseBody
	public ReturnT<String> resume(int id) {
		return xxlJobService.resume(id);
	}

	// 手动执行任务
	// 页面上点击“执行” 按钮， 前端会发送一个请求 /jobinfo/trigger   post 请求
	// param: id = 任务ID
	// controller最终会调用service的方法进行处理
	@RequestMapping("/trigger")
	@ResponseBody
	public ReturnT<String> triggerJob(int id) {
		return xxlJobService.triggerJobById(id);
	}

	@RequestMapping("/killRunningJob")
	@ResponseBody
	public ReturnT<String> killRunningJob(int id ){
		return xxlJobService.killRunningJob(id);
	}

	@RequestMapping("/getNewestJob")
	@ResponseBody
	public XxlJobInfo getNewestJob(){
		return xxlJobService.getNewestJob();
	}
}
