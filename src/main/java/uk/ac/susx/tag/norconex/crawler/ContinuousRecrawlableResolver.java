package uk.ac.susx.tag.norconex.crawler;

import com.norconex.collector.http.recrawl.PreviousCrawlData;
import com.norconex.collector.http.recrawl.impl.GenericRecrawlableResolver;

import uk.ac.susx.tag.norconex.document.ContinuousHttpDocument.ContinuousPreviousCrawlData;

/**
 * Used to calculate how long to delay (in milliseconds) this sites recrawl.
 * Not to be confused with crawl priority
 * @author jp242
 */
public class ContinuousRecrawlableResolver extends GenericRecrawlableResolver {
	
	
	// set minimum to 24hrs to begin
	// crawl the site completely once to begin the process 
	// 				have a check that deals with the first crawl special case
	// only adapt delay after 3rd crawl of site
	// implement no parents, no loops, no out of domain/url regex
	// checksum that checks if page has changed. 
	// 			if forum scrape then check the posts
	//			if page check the overall content of the page
	//					- what about boilerplate?
	// 
	
	
	/**
	 * Tough part of implementation!!
	 * @param doc
	 * @return
	 */
	public  long calculateDelay(PreviousCrawlData doc) {
		
		return 0l;
	}
	
	
	
	public void setUseSiteMap(boolean site) {
		if(site) {
			this.setSitemapSupport(SitemapSupport.FIRST);
		}
		else{
			this.setSitemapSupport(SitemapSupport.NEVER);
		}
	}
	

}
