package iqiyi;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;

public class Crawler implements PageProcessor {
	private Site site = Site.me().setTimeOut(4000).setRetryTimes(4).setSleepTime(1000).setUserAgent(
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

	public static final String ALBUMID_RULE = "albumId:(.+?\\d),";
	public static final String LIST_PAGE_RULE = "http://list\\.iqiyi\\.com/www/.+?\\.html";
	public static final String RESULT_RULE = "http://mixer\\.video\\.iqiyi\\.com/jp/mixin/videos/avlist\\?albumId=\\d+?&size=4096";
	public static final String VIDEO_PAGE_RULE = "http://(www|vip)\\.iqiyi\\.com/.+?\\.html";

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public void process(Page page) {
		if (page.getUrl().regex(LIST_PAGE_RULE).match()) {
			page.addTargetRequests(page.getHtml().links().regex(LIST_PAGE_RULE, 0).all());
			page.addTargetRequests(page.getHtml().links().regex(VIDEO_PAGE_RULE, 0).all());
		} else if (page.getUrl().regex(VIDEO_PAGE_RULE).match()) {
			String albumId = page.getHtml().regex("albumId:(.*?\\d),", 1).toString().replace(" ", "");
			if (StringUtils.isNotBlank(albumId)) {
				page.addTargetRequest(
						"http://mixer.video.iqiyi.com/jp/mixin/videos/avlist?albumId=" + albumId + "&size=4096");
				page.addTargetRequests(page.getHtml().links().regex(VIDEO_PAGE_RULE, 0).all());
			}
		} else if (page.getUrl().regex(RESULT_RULE).match()) {
			String json = page.getJson().toString().replace("var tvInfoJs=", "");
			List<String> mixinVideos = new JsonPathSelector("$.mixinVideos").selectList(json);
			if (!mixinVideos.isEmpty()) {
				JsonPathSelector jsonPathAlbumId = new JsonPathSelector("$.albumId");
				JsonPathSelector jsonPathTvId = new JsonPathSelector("$.tvId");
				JsonPathSelector jsonPathUrl = new JsonPathSelector("$.url");
				JsonPathSelector jsonPathPlayCount = new JsonPathSelector("$.playCount");
				JsonPathSelector jsonPathName = new JsonPathSelector("$.name");
				JsonPathSelector jsonPathDescription = new JsonPathSelector("$.description");
				String record = "";
				for (Iterator<String> iterator = mixinVideos.iterator(); iterator.hasNext();) {
					String element = iterator.next();
					record += jsonPathAlbumId.select(element) + "\t" + jsonPathTvId.select(element) + "\t"
							+ jsonPathUrl.select(element) + "\t" + jsonPathPlayCount.select(element) + "\t"
							+ jsonPathName.select(element).replaceAll("[\t\n]", "") + "\t"
							+ jsonPathDescription.select(element).replaceAll("[\t\n]", " ") + "\n";
				}
				System.out.print(record);
			}
		}
	}
}