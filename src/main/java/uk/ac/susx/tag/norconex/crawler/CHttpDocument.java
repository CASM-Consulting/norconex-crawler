package uk.ac.susx.tag.norconex.crawler;

import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.commons.lang.io.CachedInputStream;

public class CHttpDocument extends HttpDocument {

	public CHttpDocument(String reference, CachedInputStream content) {
		super(reference, content);
		// TODO Auto-generated constructor stub
	}

}
