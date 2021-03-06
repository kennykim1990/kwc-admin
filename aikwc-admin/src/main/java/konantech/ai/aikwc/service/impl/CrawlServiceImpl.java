package konantech.ai.aikwc.service.impl;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import konantech.ai.aikwc.common.config.StatusWebSocketHandler;
import konantech.ai.aikwc.common.utils.CommonUtil;
import konantech.ai.aikwc.entity.Agency;
import konantech.ai.aikwc.entity.Collector;
import konantech.ai.aikwc.entity.Crawl;
import konantech.ai.aikwc.entity.collectors.BasicCollector;
import konantech.ai.aikwc.repository.CrawlRepository;
import konantech.ai.aikwc.selenium.BasicCollectorKWC;
import konantech.ai.aikwc.selenium.KWCSelenium;
import konantech.ai.aikwc.service.CollectorService;
import konantech.ai.aikwc.service.CommonService;
import konantech.ai.aikwc.service.CrawlService;

@Service("CrawlService")
public class CrawlServiceImpl implements CrawlService {
	@Resource(name = "BasicCollectorService")
	BasicCollectorServiceImpl collectorService;
	
	@Autowired
	CommonService commonService;
	
	public int callCollectorScrap(int pk, String start, String end) throws ClassNotFoundException {
		int result = 1;
		Collector selectedCollector = collectorService.getCollector(pk);
		Class collectorClass = Class.forName(selectedCollector.getPackageClassName());
		if(collectorClass.isInstance(new BasicCollector())) {
			Collector c = collectorService.getCollector(pk);
			result = collectorService.webCrawl(c,start,end);
		}else {
			
		}
		return result;
	}
	
	@Async("kwcExecutor")
	public CompletableFuture webCrawlThread(int pk, String start, String end) throws Exception {
		int result = 1;
		callCollectorScrap( pk, start, end);
		return CompletableFuture.completedFuture(result);
	}

	public int webCrawlDefault(int pk, String start, String end) throws Exception {
		int result = 1;
		callCollectorScrap( pk, start, end);
		return result;
	}
	
}
