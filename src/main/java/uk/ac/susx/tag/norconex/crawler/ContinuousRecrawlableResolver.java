package uk.ac.susx.tag.norconex.crawler;

import com.norconex.collector.http.recrawl.impl.GenericRecrawlableResolver;

import uk.ac.susx.tag.norconex.document.ContinuousHttpDocument.ContinuousPreviousCrawlData;

/**
 * Used to calculate how long to delay (in milliseconds) this sites recrawl.
 * Not to be confused with crawl priority
 * @author jp242
 */
public class ContinuousRecrawlableResolver extends GenericRecrawlableResolver {
	
	/**
	 * Tough part of implementation!! - not to be confused with crawl priority
	 * @param doc
	 * @return
	 */
	public long calculateDelay(ContinuousPreviousCrawlData doc) {
		return 0l;
	}
	
	
	public void setUseSiteMap(boolean site) {
		if(site) {
			this.setSitemapSupport(SitemapSupport.LAST);
		}
		else{
			this.setSitemapSupport(SitemapSupport.NEVER);
		}
	}
	

}
