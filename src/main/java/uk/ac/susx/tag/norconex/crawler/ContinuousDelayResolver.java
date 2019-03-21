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
	
	private Map<String,DelayReferencePattern> shedules; 
	
	public ContinuousDelayResolver() {
		this.setScope(AbstractDelayResolver.SCOPE_SITE);
	}

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

}
