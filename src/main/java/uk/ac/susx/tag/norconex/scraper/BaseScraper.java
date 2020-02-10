package uk.ac.susx.tag.norconex.scraper;

import org.jsoup.Jsoup;

import uk.ac.susx.tag.norconex.scraping.GeneralSplitterFactory;
import uk.ac.susx.tag.norconex.scraping.Post;
import uk.ac.susx.tag.norconex.utils.ScraperNotFoundException;


import java.util.*;
/**
 * General idea:
 * 1
 *  - have a scraper service sitting on a separate queue with seperate engines polling it
 *  - scraper jobs sent to queue via crawler
 *  - engines pick up the job and run through the scraper
 *  OR
 *  2
 *  - crawled pages written as files
 *  - scrapers running in the engine pick up the pages and write to disc - deleting json when done
 *
 *  2 probably better as is completely independent from the crawling architecture
 *
 */
public abstract class BaseScraper {

    public static Map<String, GeneralSplitterFactory> scrapers;

    public BaseScraper(Map<String, GeneralSplitterFactory> scrapers) {
        this.scrapers = scrapers;
    }

    /**
     * Add last-scrape validation
     * @param html
     * @param scraperName
     * @return
     * @throws ScraperNotFoundException
     */
    public LinkedList<Post> scrapePage(String html, String scraperName) throws ScraperNotFoundException {
        GeneralSplitterFactory scraper = scrapers.get(scraperName);
        if(scraper == null) {
            throw new ScraperNotFoundException(scraperName);
        }
        return scraper.create().split(Jsoup.parse(html));
    }


}
