package uk.ac.susx.tag.norconex.scraping;

/**
 * Used to instantiate 
 * Created by jp242 on 07/10/2015.
 */
public interface IForumSplitterFactory {

	public IForumSplitter create();
	
	/**
	 * @param url
	 * @return true if the given url belongs to this splitters domain
	 */
	public boolean correctDomain(String url);

}
