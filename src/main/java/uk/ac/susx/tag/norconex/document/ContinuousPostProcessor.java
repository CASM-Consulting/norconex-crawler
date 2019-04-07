package uk.ac.susx.tag.norconex.document;

// java imports
import java.util.concurrent.BlockingQueue;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// http imports
import org.apache.http.client.HttpClient;

import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.processor.IHttpDocumentProcessor;

import uk.ac.susx.tag.norconex.crawler.ContinuousStatsStore;
import uk.ac.susx.tag.norconex.crawler.ContinuousStatsStore.ContinuousMetaData;

public class ContinuousPostProcessor implements IHttpDocumentProcessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousPostProcessor.class);
	
	// queue for M52 to take from
	private final BlockingQueue<HttpDocument> queue;
	// store for updating continuous details
	private ContinuousStatsStore store;
	
	public ContinuousPostProcessor(BlockingQueue<HttpDocument> queue, ContinuousStatsStore store) {
		this.queue = queue;
		this.store = store;
	}

	@Override
	public void processDocument(HttpClient httpClient, HttpDocument doc) {
		
		// Add to queue for M52 to collect
		queue.add(doc);
		
		// Update the crawl interval estimation stats
		ContinuousMetaData meta = store.getURLMetadata(doc.getReference());
		meta.incrementChangeCount();
		meta.updateLastChanged();
		meta.estimateInterval(true);

	}

}
