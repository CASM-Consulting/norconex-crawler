package uk.ac.susx.tag.norconex.crawler;

import java.util.List;

import com.norconex.collector.http.recrawl.PreviousCrawlData;
import com.norconex.collector.http.recrawl.impl.GenericRecrawlableResolver;

/**
 * Used to control whether this site should be recrawled based on url match
 * Acts as a safety net in case new urls not in domain etc... slip the net.
 * Not to be confused with crawl priority
 * @author jp242
 */
public class ContinuousRecrawlableResolver extends GenericRecrawlableResolver {
	
	// THIS OR DELAY RESOLVER?
	private List<String> subUrls;
	private double rate;          // parameter to control the 
	
	
	public ContinuousRecrawlableResolver(List<String> regxFiltPatterns, double rate) {
		this.subUrls = regxFiltPatterns;
		this.rate = rate;
		// Set a min frequency so the crawler doesn't run away and over crawl a site
//		this.setMinFrequencies(new MinFrequency);
	}
	

	// implementation of continuous delay
	public long calculateFreshnessDelay(PreviousCrawlData prevCrawl, long delay) {
		
		long numChanges = Long.valueOf(prevCrawl.getSitemapChangeFreq());
		
		long priority = 0l;
		
		long newDelay = 0l;
		
		
		
		return newDelay;
	}
	
	public long estimateFreshness(PreviousCrawlData prevCrawl) {
		return 0l;
	}
	
	public ContinuousRecrawlableResolver() {}
	
	
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
	
	
	@Override
    public boolean isRecrawlable(PreviousCrawlData prevData) {
        
		if(subUrls != null) {
        	for(String patt : subUrls) {
        		if(prevData.getReference().matches(patt)) {
        			return true;
        		}
        	}
        }
        
		return super.isRecrawlable(prevData);
	}

}
