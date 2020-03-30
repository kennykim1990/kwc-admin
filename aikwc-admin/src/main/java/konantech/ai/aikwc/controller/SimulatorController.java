package konantech.ai.aikwc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import konantech.ai.aikwc.common.config.AsyncConfig;
import konantech.ai.aikwc.common.config.StatusWebSocketHandler;
import konantech.ai.aikwc.entity.Agency;
import konantech.ai.aikwc.entity.Collector;
import konantech.ai.aikwc.entity.KLog;
import konantech.ai.aikwc.entity.KTask;
import konantech.ai.aikwc.service.CollectorService;
import konantech.ai.aikwc.service.CommonService;
import konantech.ai.aikwc.service.CrawlService;
import konantech.ai.aikwc.service.ScheduleService;

@Controller
@RequestMapping("simulator")
public class SimulatorController {
	@Resource
	CommonService commonService;
	
	@Resource
	CollectorService collectorService;
	@Autowired
	CrawlService crawlService;
	@Autowired
	AsyncConfig asyncConfig;
	@Autowired
	StatusWebSocketHandler statusHandler;
	
	@Autowired
	ScheduleService scheduleService;
	
	@RequestMapping("/list")
	public String list(@RequestParam(name = "agencyNo", required = false, defaultValue = "0") Integer agencyNo
			,Model model) {
		Map map = commonService.commInfo(agencyNo);
		Agency selAgency = (Agency) map.get("selAgency");
		model.addAttribute("selAgency", selAgency);
		model.addAttribute("agencyList", map.get("agencyList"));
		model.addAttribute("groupList", map.get("groupList"));
		model.addAttribute("agencyNo", selAgency.getPk());
		model.addAttribute("menuNo", "1");
		
		return "sml/runJob";
	}
	
	@RequestMapping("/schedule")
	public String runSchedule(@RequestParam(name = "agencyNo", required = false, defaultValue = "0") Integer agencyNo
			,@RequestParam(name = "menuNo", required = false, defaultValue = "1") String menuNo
			,Model model) {
		Map map = commonService.commInfo(agencyNo);
		Agency selAgency = (Agency) map.get("selAgency");
		model.addAttribute("selAgency", selAgency);
		model.addAttribute("agencyList", map.get("agencyList"));
		model.addAttribute("groupList", map.get("groupList"));
		model.addAttribute("agencyNo", selAgency.getPk());
		model.addAttribute("menuNo", "2");
		
		return "sml/runSchedule";
	}
	@RequestMapping("/log")
	public String viewLog(@RequestParam(name = "agencyNo", required = false, defaultValue = "0") Integer agencyNo
			,@RequestParam(name = "menuNo", required = false, defaultValue = "1") String menuNo
			,Model model) {
		
		Map map = commonService.commInfo(agencyNo);
		Agency selAgency = (Agency) map.get("selAgency");
		model.addAttribute("selAgency", selAgency);
		model.addAttribute("agencyList", map.get("agencyList"));
		model.addAttribute("groupList", map.get("groupList"));
		model.addAttribute("agencyNo", selAgency.getPk());
		model.addAttribute("menuNo", "3");
		
		List<KLog> logList = new ArrayList<KLog>();
		logList = commonService.getAgencyLogList(selAgency.getStrPk());
		model.addAttribute("logList", logList);
		return "sml/viewLog";
	}
	
	
	
	@RequestMapping(value ="/collectorList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> collectorList(@RequestBody Map<String,String> params) {
		
		String agency = params.get("agencyNo");
		List<Collector> result = new ArrayList<Collector>();
		if(agency != null && !agency.equals("")) {
			result = collectorService.getCollectorListInAgency(Integer.parseInt(agency));
		}else
			result = collectorService.getCollectorList();
			
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("result", result);
		map.put("taskCnt", asyncConfig.getTaskCount());
		
		return map;
	}
	
	@RequestMapping("/crawl")
	@ResponseBody
	public void getCrawl(@RequestBody Collector collector) throws Exception {
		
		Collector selectedCollector = collectorService.getCollectorInfo(collector.getPk());
		selectedCollector.setStartPage(collector.getStartPage());
		selectedCollector.setEndPage(collector.getEndPage());
		
		Agency Agency = collectorService.getAgencyNameForCollector(selectedCollector.getToSite().getGroup().getAgency());
		String agencyName = Agency.getName();
		selectedCollector.getToSite().getGroup().setAgencyName(agencyName);
		selectedCollector.setChannel("기관");
		
		//1. update Running status / send websocket message
		collectorService.updateStatus(collector.getPk(), "R");
		
		CompletableFuture cf = crawlService.webCrawl(selectedCollector);
		statusHandler.sendTaskCnt(asyncConfig.getTaskCount());
		
		CompletableFuture<Void> after = cf.handle((res,ex) -> {
			statusHandler.sendTaskCnt(asyncConfig.getAfterTaskCount());
			return null;
		});
		
	}
	
	@RequestMapping(value="/schedule/save", method = RequestMethod.POST)
	public String saveSchdule(@ModelAttribute KTask task) throws Exception {
//		KTask task = new KTask();
		task.setTaskNo("crawlTask");
		scheduleService.registerSchedule(task,"*/10 * * * * *");
		
		return "redirect:/simulator/schedule";
	}
	
	@RequestMapping(value="/schedule/delete", method = RequestMethod.POST)
	@ResponseBody
	public void deleteSchdule() throws Exception {
		KTask task = new KTask();
		task.setTaskNo("crawlTask");
		scheduleService.stopSchedule(task);
	}
}

