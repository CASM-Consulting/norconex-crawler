package uk.ac.susx.tag.norconex.crawlstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.crawler.ContinuousRecrawlableResolver;

import java.io.Serializable;

/**
 * A single set of stats for a single url.
 * Used to estimate the interval that should be left until the next crawl.
 * @author jp242
 *
 */
public class ContinuousMetadata implements Serializable {

    protected static final Logger logger = LoggerFactory.getLogger(ContinuousRecrawlableResolver.class);

    private long checkedCount;
    private long changeCount;
    private long nextCrawl;


    public ContinuousMetadata() {
        checkedCount = 1;
        changeCount = 0;
        nextCrawl = 0;
    }

    public void incrementChangeCount() {

        changeCount++;

    }

    public void incrementCheckedCount() {
        checkedCount++;
    }

    public long getCheckedCount(){ return checkedCount; }

    public long getChangeCount() { return changeCount; }

    public long getNextCrawl() {
        return nextCrawl;
    }

    public void setNextCrawl(long crawl) {
        if(crawl > nextCrawl) {
            nextCrawl = crawl;
        }
    }

}
