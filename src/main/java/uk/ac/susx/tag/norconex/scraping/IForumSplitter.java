package uk.ac.susx.tag.norconex.scraping;

// java imports
import org.jsoup.nodes.Document;

import java.util.LinkedList;

/**
 * Created by jp242 on 07/10/2015.
 */
public interface IForumSplitter {

	/**
	 * Retrieves individual forum posts that may be present in the page.
	 * @param doc jsoup Document object representing the web page.
	 * @return Linked list of forum posts
	 */
	public LinkedList<Post> split(Document doc);
	
	/**
	 * @return Map the meta-fields contained in the configuration to the tags existing with the forum.
	 */
	public void mapFields(LinkedList<Post> posts);
	

}
