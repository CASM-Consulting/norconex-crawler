package uk.ac.susx.tag.norconex.utils;

public class IncorrectScraperJSONException extends Exception {

    public IncorrectScraperJSONException() {
        super("Poorly formed json for scraper found");
    }

}
