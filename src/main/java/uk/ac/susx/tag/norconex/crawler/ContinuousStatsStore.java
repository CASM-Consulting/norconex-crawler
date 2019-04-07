package uk.ac.susx.tag.norconex.crawler;

// mv store stats
import org.h2.mvstore.*;

// logging stats
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.susx.tag.norconex.controller.ContinuousController;

/**
 * Store used to contain a cached object for each url that specifies it's crawl stats for use in calculating 
 * recrawl delay.
 * @author jp242
 *
 */
public class ContinuousStatsStore {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousStatsStore.class);

	private MVStore cacheStore;
	private MVMap<String,ContinuousMetaData> continuousCache;
	private static final double offset = 0.5;
	
	public static final String CONMAP = "continuousCache";
	
	public ContinuousStatsStore(String cacheStore) {
		this.cacheStore = MVStore.open(cacheStore);
		continuousCache = this.cacheStore.openMap(CONMAP);
	}
	
	/**
	 * Close the cache and release resources
	 */
	public void close() {
		cacheStore.close();
	}
	
	/**
	 * Get the metadata for the specified url
	 * @param reference
	 * @return
	 */
	public ContinuousMetaData getURLMetadata(String reference) {
		if(continuousCache.containsKey(reference)) {
			return continuousCache.get(reference);
		}
		return null;
	}

	/**
	 * Creates new metadata for a url not stored
	 * @param reference
	 * @return
	 */
	public ContinuousMetaData addMetadata(String reference) {
		if(!continuousCache.containsKey(reference)) {
			ContinuousMetaData metadata = new ContinuousMetaData();
			continuousCache.put(reference, metadata);
			return metadata;
		}
		return continuousCache.get(reference);
	}
	
	/**
	 * A single set of stats for a single url. 
	 * Used to estimate the interval that should be left until the next crawl.
	 * @author jp242
	 *
	 */
	public class ContinuousMetaData {
		
		private long checkedCount;
		private long changedCount;
		private long sumofTimesChanged;
		
		private long lastChecked;
		private long lastChanged;
		private long nextCrawl;
//		private long totalTime;
		
		public ContinuousMetaData() {
			checkedCount = 0;
			changedCount = 0;
			sumofTimesChanged = 0;
			
			nextCrawl = 0;
//			totalTime = 0;
			lastChanged = System.currentTimeMillis();
			lastChecked = System.currentTimeMillis();
		}
		
		public void incrementChangeCount() {
			changedCount++;
		}
		
		public void incrementCheckedCount() {
			checkedCount++;
		}
		
		public long getNextCrawl() {
			return nextCrawl;
		}
		
		public long getcheckedCount() {
			return checkedCount;
		}
		
		public long getLastChanged() {
			return lastChanged;
		}
		
		public void updateLastChanged() {
			lastChanged = System.currentTimeMillis();
		}
		
		public void estimateInterval(boolean changed) {
			
			if(changed) {
				changedCount++;
				sumofTimesChanged -= (System.currentTimeMillis() - lastChecked); //Needed as this is done in post when this value would have already been accessed
				sumofTimesChanged += (System.currentTimeMillis() - lastChanged);
			}
			else { 
				sumofTimesChanged += (System.currentTimeMillis() - lastChecked);
			}
						
			double interval = (changedCount/sumofTimesChanged);
			
			// estimate interval
			if(interval < ContinuousController.DEFAULT_MIN_RECRAWL_DELAY) {
				nextCrawl = System.currentTimeMillis() + ContinuousController.getDelay(ContinuousController.Delay.MINIMUM);
				return;
			}
			if(interval > ContinuousController.DEFAULT_MAX_RECRAWL_DELAY) {
				nextCrawl = System.currentTimeMillis() + ContinuousController.getDelay(ContinuousController.Delay.MAXIMUM);
				return;
			}
			else {
				nextCrawl = System.currentTimeMillis() + Math.round(interval);
			}
			
		}

	}
	
	public static void main(String[] args) {
		long totalChecked = 20;
		long timesChanged = 2;
		System.out.println(-Math.log((totalChecked-timesChanged)+offset/(totalChecked)+offset));
	}
	
	
}
