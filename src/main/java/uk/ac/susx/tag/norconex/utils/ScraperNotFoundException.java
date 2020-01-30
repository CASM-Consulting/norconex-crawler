package uk.ac.susx.tag.norconex.utils;

public class ScraperNotFoundException extends Exception {
    public ScraperNotFoundException(String domain) {
        super("No scraper found for the domain: " + domain);
    }
}
