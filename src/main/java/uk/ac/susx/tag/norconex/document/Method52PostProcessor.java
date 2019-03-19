package uk.ac.susx.tag.norconex.document;

import java.util.concurrent.BlockingQueue;

import org.apache.http.client.HttpClient;

import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.processor.IHttpDocumentProcessor;

public class Method52PostProcessor implements IHttpDocumentProcessor {
	
	private final boolean forum;
	private final BlockingQueue<HttpDocument> queue;
	
	public Method52PostProcessor(boolean forum, BlockingQueue<HttpDocument> queue) {
		this.forum = forum;
		this.queue = queue;
	}
	
	public Method52PostProcessor(BlockingQueue<HttpDocument> queue) {
		this(false,queue);
	}

	@Override
	public void processDocument(HttpClient httpClient, HttpDocument doc) {
		queue.add(doc);
		if(forum) {
			processForum(doc);
		}
	}
	
	private void processForum(HttpDocument doc ) {}

}
