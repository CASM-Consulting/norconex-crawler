package uk.ac.susx.tag.norconex.crawler;

import java.util.Map;

import com.norconex.collector.http.delay.impl.AbstractDelayResolver;
import com.norconex.collector.http.delay.impl.ReferenceDelayResolver.DelayReferencePattern;
import com.norconex.collector.http.recrawl.PreviousCrawlData;

/**
 * Creates a delay specific to a url that specifies how often that page should be crawled for new info.
 * @author jp242
 */
public class ContinuousDelayResolver extends AbstractDelayResolver {
	
	private static final long defaultDelay = 86000000;
	private Map<String,DelayReferencePattern> shedules; 

	@Override
	protected long resolveExplicitDelay(String url) {
		if(shedules.get(url) == null) {
			return getDefaultDelay();
		}
		return shedules.get(url).getDelay();
	}
	
	public void addSchedule(String url, long delay) {
		shedules.put(url, new DelayReferencePattern(url,delay));
	}

	
	public long calculateSchedule(PreviousCrawlData prevCrawl) {
		return 0l;
	}
}
