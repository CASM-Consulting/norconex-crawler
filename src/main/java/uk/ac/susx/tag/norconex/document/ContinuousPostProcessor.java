package uk.ac.susx.tag.norconex.document;

// http imports
import org.apache.http.client.HttpClient;

import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.processor.IHttpDocumentProcessor;

import uk.ac.susx.tag.norconex.controller.ContinuousController;
import uk.ac.susx.tag.norconex.crawlstore.ContinuousEstimatorStore;
import uk.ac.susx.tag.norconex.crawlstore.ContinuousMetadata;

public class ContinuousPostProcessor implements IHttpDocumentProcessor {

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
		if(meta.getCheckedCount() >= ContinuousController.BURNIN_CRAWLS) {
			meta.setNextCrawl(store.estimateCrawlInterval(store.estimateChangeFrequency(meta.getCheckedCount(),meta.getChangeCount())));
		}

	}

}
