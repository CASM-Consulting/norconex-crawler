package uk.ac.susx.tag.norconex.document;

import java.util.concurrent.BlockingQueue;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.processor.IHttpDocumentProcessor;

public class Method52PostProcessor implements IHttpDocumentProcessor  {
	
protected static final Logger logger = LoggerFactory.getLogger(ContinuousPostProcessor.class);
	
	// queue for M52 to take from
	private final BlockingQueue<HttpDocument> queue;
	
	public Method52PostProcessor(BlockingQueue<HttpDocument> queue) {
		this.queue = queue;
	}

	@Override
	public void processDocument(HttpClient httpClient, HttpDocument doc) {
		
		// Add to queue for M52 to collect
		queue.add(doc);
		
	}

}
