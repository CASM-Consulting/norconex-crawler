package uk.ac.susx.tag.norconex.collector;

import com.norconex.collector.core.AbstractCollector;
import com.norconex.collector.core.ICollectorConfig;
import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.crawler.ICrawlerConfig;

public class ContinuousCollector extends AbstractCollector {

	public ContinuousCollector(ICollectorConfig collectorConfig) {
		super(collectorConfig);
	}

	@Override
	protected ICrawler createCrawler(ICrawlerConfig config) {
		return null;
	}

}
