package uk.ac.susx.tag.norconex.scraping;

/**
 * Global values shared between communicating classes, Nutch and Solr.
 * @author jp242
 *
 */
public class GlobalFieldValues {
	
	// Document id fields
	public static final String ID = "id";
	public static final String POST_ID = "post_id";
	public static final String THREAD_ID = "thread_id";
	
	//Post splitting fields
	public static final String POST_FIELD = "post";	// The field name used to index the forum-post meta-data.
	public static final String NUM_POSTS = "numposts";	// The field name used to index the number of forum posts stored in a page.
	public static final String POSITION = "position";
	public static final String CONTENT = "postcontent"; // The textual post content
	
	// Post tag fields
	public static final String MEMBER = "member";
	public static final String MEMBER_POSTS = "memberposts";	
	public static final String MEM_SINCE = "membsince";
	public static final String TITLE = "title";
	public static final String LOCATION = "location";
	public static final String THANKS = "thanks";
	public static final String QUOTE_MEM = "quoted_member";
	public static final String QUOTE_DATE = "quoted_date";
	
	// Filter fields
	public static final String PAGE_NUMBER = "page";    // The field name used to index the page number if there is no pagination start value
	public static final String PAGE_START = "pagestart";// The field name used to index the pagination start point.
	public static final String PAGE_END = "pageend";	// The field name used to index the pagination end point.
	public static final String BASE_URL = "baseurl"; 	// The field name used to index the base url of a page.
	public static final String SUBJECT = "subject";		// The field name used to index the subject of the forum thread.
	public static final String QUESTION = "question";	// The field name used to index the question being asked in the forum.
	public static final String LINKS = "links";
	public static final String POST_DATE = "postdate";
		
}
