package uk.ac.susx.tag.norconex.document;

import java.util.concurrent.BlockingQueue;

import com.norconex.commons.lang.file.ContentType;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.processor.IHttpDocumentProcessor;

public class Method52PostProcessor implements IHttpDocumentProcessor  {

	// queue for M52 to take from
	private final BlockingQueue<HttpDocument> queue;
	
	public Method52PostProcessor(BlockingQueue<HttpDocument> queue) {
		this.queue = queue;
	}

	@Override
	public void processDocument(HttpClient httpClient, HttpDocument doc) {

		if(isText(doc)) {
			// Add to queue for M52 to collect
			queue.add(doc);
		}
	}

	// Check this is text based only content for M52
	public boolean isText(HttpDocument doc) {
		ContentType ct = doc.getContentType();
		String contenFam = ct.getContentFamily().getId();
		return (ContentType.TEXT.getContentFamily().getId().equals(contenFam) || ContentType.HTML.getContentFamily().getId().equals(contenFam) ||
				ContentType.CSV.getContentFamily().getId().equals(contenFam) || ContentType.XML.getContentFamily().getId().equals(contenFam)) ?
				true : false;
	}

}
