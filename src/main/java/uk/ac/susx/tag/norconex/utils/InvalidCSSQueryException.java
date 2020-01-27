package uk.ac.susx.tag.norconex.utils;

public class InvalidCSSQueryException extends Exception {
	
	private static final String MESSAGE = "The css query was incorrectly formatted ";
	
	public InvalidCSSQueryException(String cssTag, String value) {
		
		super(constructMessage(cssTag,value));
		
	}
	
	private static String constructMessage(String cssTag, String value) {
		
		StringBuilder builder = new StringBuilder().append(MESSAGE);
		
		if(cssTag == "attribute") {
			return builder.append("An attribute value was requested however the attribute was not found or was poorly formed.").toString();
		}
		
		return MESSAGE;
	}

}
