package uk.ac.susx.tag.norconex.crawler;

// mv store stats
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.h2.mvstore.*;

// logging stats
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.controller.ContinuousController;

import java.io.File;

/**
 * Store used to contain a cached object for each url that specifies it's crawl stats for use in calculating 
 * recrawl delay.
 * @author jp242
 *
 */
public class ContinuousEstimatorStore {

	protected static final Logger logger = LoggerFactory.getLogger(ContinuousController.class);

	private MVStore cacheStore;
	private MVMap<String, ContinuousMetadata> continuousCache;
	private MVMap<String,GlobalMetadata> globalCache;
	private static final double RATE = 0.5;
	
	public static final String CONMAP = "continuousCache";
	public static final String GLOBALMAP = "globalMap";
	public static final String GLOBALKEY = "globalStats";
	
	public ContinuousEstimatorStore(String cacheStore) {

		// makesure db is avail
		File storeParent = new File(cacheStore).getParentFile();
		if(!storeParent.exists()) {
			storeParent.mkdirs();
		}

		this.cacheStore = MVStore.open(cacheStore);
		continuousCache = this.cacheStore.openMap(CONMAP);
		globalCache = this.cacheStore.openMap(GLOBALMAP);
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
	public ContinuousMetadata getURLMetadata(String reference) {
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
	public ContinuousMetadata addMetadata(String reference) {
		if(!continuousCache.containsKey(reference)) {
			ContinuousMetadata metadata = new ContinuousMetadata();
			continuousCache.put(reference, metadata);
			return metadata;
		}
		return continuousCache.get(reference);
	}

	public GlobalMetadata getGlobalMetadata() {
		if(!globalCache.containsKey(GLOBALKEY)) {
			GlobalMetadata gm = new GlobalMetadata();
			globalCache.put(GLOBALKEY,gm);
		}
		return globalCache.get(GLOBALKEY);
	}


	/**
	 * Contains information about the entire continuous crawl of the site
	 */
	public class GlobalMetadata {

		private long totalCrawls;
		private long totalTime;
		private long lastCrawled;

		public GlobalMetadata() {
			totalCrawls = 1;
			totalTime = 1;
			lastCrawled = System.currentTimeMillis();
		}

		public void incrementCrawls() {
			totalCrawls++;
			logger.info("INFO: TOTAL CRAWLS: " + totalCrawls);
		}

		/**
		 * Adds the latest interval to the total crawl time, unless the crawl time is more than twice the average time
		 * i.e. crawler might have been turned off for a while
		 * Then the average time is added to the total time.
		 */
		public void updateCrawlTime() {
			long lastInterval = (System.currentTimeMillis() - lastCrawled);
			if(totalCrawls < ContinuousController.BURNIN_CRAWLS) {
				totalTime += lastInterval;
			}
			else {
				if(lastInterval > (averageCrawl() * 2)) {
					totalTime += averageCrawl();
				}
				else {
					totalTime += lastInterval;
				}
			}
			lastCrawled = System.currentTimeMillis();
		}

		public long averageCrawl() {
			return totalTime / totalCrawls;
		}

	}
	
	/**
	 * A single set of stats for a single url. 
	 * Used to estimate the interval that should be left until the next crawl.
	 * @author jp242
	 *
	 */
	public class ContinuousMetadata {
		
		private long checkedCount;
		private long changeCount;
		private long nextCrawl;


		public ContinuousMetadata() {
			checkedCount = 0;
			changeCount = 0;
			nextCrawl = 0;
		}
		
		public void incrementChangeCount() {
			changeCount++;
		}

		public void incrementCheckedCount() {
			checkedCount++;
		}

		public long getCheckedCount(){ return checkedCount; }

		public long getChangeCount() { return changeCount; }

		public long getNextCrawl() {
			return nextCrawl;
		}

		public void setNextCrawl(long crawl) {
			if(crawl > nextCrawl) {
				nextCrawl = crawl;
			}
		}

	}

	/**
	 * @param checkedCount
	 * @param changeCount
	 * @return Estimates a pages likelihood of change between checks of the site based on number of times checked and changed
	 */
	public double estimateChangeFrequency(double checkedCount, double changeCount) {
		return Math.log((checkedCount - changeCount) + RATE / checkedCount + RATE) / Math.log(2);
	}

	/**
	 * @param rateOfChange
	 * @return Estimates the amount of time before the page should be checked again as a proportion of the number of times checked
	 * and the average time a complete interation of a crawl takes.
	 */
	public long estimateCrawlInterval(double rateOfChange) {
		double interval = rateOfChange * globalCache.get(GLOBALKEY).averageCrawl();
		if(interval < ContinuousController.DEFAULT_MIN_RECRAWL_DELAY) {
			return Math.round(System.currentTimeMillis() + ContinuousController.getDelay(ContinuousController.Delay.MINIMUM));
		}
		if(interval > ContinuousController.DEFAULT_MAX_RECRAWL_DELAY) {
			return Math.round(System.currentTimeMillis() + ContinuousController.getDelay(ContinuousController.Delay.MAXIMUM));
		}
		return Math.round(interval + System.currentTimeMillis());
	}

}
