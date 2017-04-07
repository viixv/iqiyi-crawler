package iqiyi;

import javax.management.JMException;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.pipeline.FilePipeline;

public class Main {
	public static void main(String[] args) throws JMException {
		int threadNumber = Runtime.getRuntime().availableProcessors();
		Spider spider = Spider.create(new Crawler()).addUrl("http://list.iqiyi.com/www/1/----------------iqiyi--.html")
				.addPipeline(new FilePipeline("/root/iqiyi")).thread(threadNumber * 16);
		SpiderMonitor.instance().register(spider);
		spider.start();
	}
}