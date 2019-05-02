package uk.ac.susx.tag.norconex.crawlstore;


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

	protected static final Logger logger = LoggerFactory.getLogger(ContinuousEstimatorStore.class);

	private MVStore cacheStore;
	private MVMap<String, ContinuousMetadata> continuousCache;
	private MVMap<String,GlobalMetadata> globalCache;
	private static final double RATE = 0.5;
	
	public static final String CONMAP = "continuousCache";
	public static final String MAPCACHE = "mapCache";
	public static final String GLOBALMAP = "globalMap";
	public static final String GLOBALKEY = "globalStats";
	
	public ContinuousEstimatorStore(String cacheStore) {


		initStore(cacheStore);

	}

	private void initStore(String storeLocation) throws RuntimeException {
		try {

			// makesure db is avail
			File storeParent = new File(storeLocation);
			if(!storeParent.exists()) {
				storeParent.mkdirs();
			}

			cacheStore = new MVStore.Builder().fileName(new File(new File(storeLocation), MAPCACHE).getAbsolutePath()).open();
			continuousCache = cacheStore.openMap(CONMAP);
			globalCache = cacheStore.openMap(GLOBALMAP);
		}
		catch (Exception e) {
			throw new RuntimeException("ERROR: Failed when attempting to open the continuous crawler cache at: " + new File(new File(storeLocation), MAPCACHE).getAbsolutePath());
		}
	}

	/**
	 * Wrappers for underlying store
	 */
	public void close() {
		cacheStore.close();
	}

	public void commit() { cacheStore.commit(); }
	
	/**
	 * Get the metadata for the specified url
	 * @param reference
	 * @return
	 */
	public ContinuousMetadata getURLMetadata(String reference) {

		if(!continuousCache.containsKey(reference)) {
			addMetadata(reference);
		}
		return continuousCache.get(reference);

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
		if (interval < ContinuousController.getDelay(ContinuousController.Delay.MINIMUM)) {
			return System.currentTimeMillis() + ContinuousController.getDelay(ContinuousController.Delay.MINIMUM);
		}
		if (interval > ContinuousController.getDelay(ContinuousController.Delay.MAXIMUM)) {
			return System.currentTimeMillis() + ContinuousController.getDelay(ContinuousController.Delay.MAXIMUM);
		}

		return Math.round(interval) + System.currentTimeMillis();
	}

}
