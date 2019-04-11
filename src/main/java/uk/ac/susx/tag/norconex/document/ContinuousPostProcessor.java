package uk.ac.susx.tag.norconex.document;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// http imports
import org.apache.http.client.HttpClient;

import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.processor.IHttpDocumentProcessor;

import uk.ac.susx.tag.norconex.crawler.ContinuousEstimatorStore;
import uk.ac.susx.tag.norconex.crawler.ContinuousEstimatorStore.ContinuousMetadata;

public class ContinuousPostProcessor implements IHttpDocumentProcessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousPostProcessor.class);
	
	// store for updating continuous details
	private ContinuousEstimatorStore store;
	
	public ContinuousPostProcessor(ContinuousEstimatorStore store) {
		this.store = store;
	}

	@Override
	public void processDocument(HttpClient httpClient, HttpDocument doc) {

		// Update the crawl interval estimation stats
		ContinuousMetadata meta = store.getURLMetadata(doc.getReference());
		meta.incrementChangeCount();
		meta.setNextCrawl(store.estimateCrawlInterval(store.estimateChangeFrequency(meta.getCheckedCount(),meta.getChangeCount())));

	}

}
