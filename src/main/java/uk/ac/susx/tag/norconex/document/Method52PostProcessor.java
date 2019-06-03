package uk.ac.susx.tag.norconex.document;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.BlockingQueue;

import com.google.gson.Gson;
import com.norconex.collector.http.doc.HttpMetadata;
import com.norconex.commons.lang.file.ContentType;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;

import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.processor.IHttpDocumentProcessor;


public class Method52PostProcessor implements IHttpDocumentProcessor  {

	// queue for M52 to take from
	private final BlockingQueue<String> queue;

	private final Gson gson;


	public Method52PostProcessor(BlockingQueue<String> queue) {
		this.queue = queue;
		this.gson = new Gson();
	}

	@Override
	public void processDocument(HttpClient httpClient, HttpDocument doc) {

		if(isText(doc)) {

			final String url = doc.getReference();

			StringWriter sw = new StringWriter();
			doc.getContent().rewind();
			try {
				IOUtils.copy(doc.getContent(), sw, doc.getContentEncoding());
			} catch (IOException e) {
				throw new RuntimeException("ERROR: Failed to retrieve web content for url: " + url);
			}

			final String html = sw.toString();

			final String parent = doc.getMetadata().getString(HttpMetadata.COLLECTOR_REFERRER_REFERENCE);
			final int depth = doc.getMetadata().getInt(HttpMetadata.COLLECTOR_DEPTH);

			final WebPage webPage = new WebPage(url,html,parent,depth);

			String json = gson.toJson(webPage);

			// Add to queue for M52 to collect
			queue.add(json);

		}

	}

	/**
	 * Class used to serialise web page metadata to json
	 */
	public class WebPage {

		private final String url;
		private final String html;
		private final String parent;
		private final int depth;

		public WebPage(String url, String html, String parent, int depth) {
			this.url = url;
			this.html = html;
			this.parent = parent;
			this.depth = depth;
		}

		public String getUrl() {return url;}
		public String getHtml() {return html;}
		public String getParent() {return parent;}
		public int getDepth() {return depth;}

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
