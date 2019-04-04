package uk.ac.susx.tag.norconex.crawler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.http.delay.impl.AbstractDelayResolver;
import com.norconex.collector.http.delay.impl.ReferenceDelayResolver.DelayReferencePattern;

/**
 * Creates a delay specific to a url that specifies how often that page should be crawled for new info.
 * 
 * DELAY FOR POLLIONG THE SITE - SIMPLE POLITENESS DELAY
 * @author jp242
 */
public class ContinuousDelayResolver extends AbstractDelayResolver {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousDelayResolver.class);
	
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
