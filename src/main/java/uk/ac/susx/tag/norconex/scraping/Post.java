package uk.ac.susx.tag.norconex.scraping;

// java imports

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// google imports

/**
 * Represents a single post in a forum thread.
 */
public final class Post extends HashMap<String,List<String>>{
	
	private static final long serialVersionUID = -976426482036410163L;
	public static final String POSTHTML = "posthtml";
	
	/**
	 * Private, empty constructor for @Gson "(de)serialization"
	 */
	private Post(){ }
	
	/**
	 * For standard instantiation purposes.
	 * @param postHTML full post, html expected
	 * @param content post content, plain-text expected
	 */
	public Post(String postHTML, String content) {
		this.put(GlobalFieldValues.CONTENT,content);
		this.put(POSTHTML, postHTML);
	}
	
	/**
	 * Convenience method for adding a single item to the fields
	 * @param key
	 * @param value
	 */
	public void put(String key, String value) {
		if(this.get(key) == null) {
			this.put(key, new ArrayList<String>());
			this.get(key).add(value);
		}
		else {
			this.get(key).add(value);
		}
	}
	
	/**
	 * @return The entire post in its html form.
	 */
	public String postHTML() {
		return this.get(POSTHTML).get(0);
	}
	
	/**
	 * @return The plain text content of the forum post.
	 */
	public String content() {
		return this.get(GlobalFieldValues.CONTENT).get(0);
	}
	
	/**
	 * @return @Post as a json string
	 */
	public String toJson() {
		final Gson gson = new Gson();
		final JsonElement tree = gson.toJsonTree(this);
		return gson.toJson(tree);
	}
	
	@Override
	public String toString() {
		return this.toJson();
	}
	
//	public static void main(String[] args) {
//		
//		Post p = new Post("FISH","WEASEL");
//		p.put("WIGGLE", "CHICKEN");
//		p.put("WIGGLE", "RABBIT");
//		p.put("MOUSE", "CAT");
//		
//		String json = p.toJson();
//		
//		System.out.println(p.toString());
//	}
	
}
