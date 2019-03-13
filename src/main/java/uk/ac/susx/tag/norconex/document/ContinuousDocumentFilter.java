package uk.ac.susx.tag.norconex.document;

import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.importer.doc.ImporterDocument;

/**
 * Used to enforce specific requirements of an document to ensure only very specific pages are crawled continuous (e.g. those of a particular sub-domain of a site)
 * @author jp242
 */
public class ContinuousDocumentFilter implements IDocumentFilter {
	
	
	public ContinuousDocumentFilter() {
		
	}

	@Override
	public boolean acceptDocument(ImporterDocument document) {
		// TODO Auto-generated method stub
		return false;
	}

}
