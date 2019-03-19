package uk.ac.susx.tag.norconex.document;

import java.util.Comparator;

//import com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.recrawl.PreviousCrawlData;
import com.norconex.commons.lang.io.CachedInputStream;

/**
 * Used to set and get specific metadata on the page used to set and calculate its priority within the re-crawl queue. 
 * @author jp242
 */
public class ContinuousHttpDocument extends HttpDocument {
	
//	private static final String CHECKSUM = "cont_checksum";
	
	// only keep this if we change checksum in some way
//	private static MD5DocumentChecksummer checker = new MD5DocumentChecksummer();

	
	public ContinuousHttpDocument(String reference, CachedInputStream content) {
		super(reference, content);
//		recrawl = true;
		
	}
	

	
//	/**
//	 * Set and get checksum so a page can be analysed for change
//	 */
//	public void setChecksum() {
//		super.getMetadata().setString(CHECKSUM, checker.doCreateDocumentChecksum(this));
//	}
//	
//	public String getChecksum() {
//		return super.getMetadata().getString(CHECKSUM);
//	}
	

	

	
	/**
	 * Check if this is used specifically to persist data across crawl runs.
	 * @author jp242
	 *
	 */
	public class ContinuousPreviousCrawlData extends PreviousCrawlData {
		
		private boolean recrawl;			// does this page fall within the specified recrawl scope
		private double  priority;

		/**
		 * Once a page recrawl is set to false it cannot be changed. 
		 * @param recrawl
		 */
		public void setRecrawl(boolean recrawl) {
			this.recrawl = (this.recrawl == false) ? false : recrawl; 
		}
		
		public boolean getRecrawl() {
			return recrawl;
		}
		
		/**
		 * A priority used to rank it within the crawl queue (intentionally based on likelihood of page change etc...)
		 * @param weight
		 */
		public void setPriority(double weight) {
			priority = weight;
		}
		
		public double getPriority() {
			return priority;
		}
		
	}
	
	
//	/**
//	 * Comparator used within a priority to rank documents and impose the crawl order
//	 * @author jp242
//	 */
//	public class ContinuousDocumentComparator implements Comparator<ContinuousHttpDocument> {
//
//		@Override
//		public int compare(ContinuousHttpDocument docOne, ContinuousHttpDocument docTwo) {
//			if(docOne.getPriority() == docTwo.getPriority()) {
//				return 0;
//			}
//			return (docOne.getPriority() < docTwo.getPriority()) ? -1 : 1;
//		}
//		
//	}
	
	

}
