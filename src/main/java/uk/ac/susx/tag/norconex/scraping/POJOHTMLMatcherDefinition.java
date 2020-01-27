package uk.ac.susx.tag.norconex.scraping;

import java.util.List;
import java.util.Map;

public class POJOHTMLMatcherDefinition {

    public String field;
    public List<Map<String,String>> tags;

    public String getKey(){
        return field;
    }

    public void setKey(String value){
        field = value;
    }

    public List<Map<String,String>> getTagDefinitions(){
        return tags;
    }

}
