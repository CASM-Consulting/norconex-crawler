package uk.ac.susx.tag.norconex.crawlstore;

import com.norconex.collector.core.data.store.impl.mvstore.MVStoreCrawlDataStore;

public class ContinuousMVSCrawlStore extends MVStoreCrawlDataStore {

	public ContinuousMVSCrawlStore(String path, boolean resume) {
		super(path, resume);
	}
	
	

}
