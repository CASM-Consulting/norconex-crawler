package uk.ac.susx.tag.norconex.crawlstore;

import uk.ac.susx.tag.norconex.controller.ContinuousController;

import java.io.Serializable;

/**
 * Contains information about the entire continuous crawl of the site
 */
public class GlobalMetadata implements Serializable {

    private long totalCrawls;
    private long totalTime;
    private long lastCrawled;

    public GlobalMetadata() {
        totalCrawls = 1;
        totalTime = 1;
        lastCrawled = System.currentTimeMillis();
    }

    public void incrementCrawls() {
        totalCrawls++;
    }

    public long getTotalCrawls() {
        return totalCrawls;
    }

    /**
     * Adds the latest interval to the total crawl time, unless the crawl time is more than twice the average time
     * i.e. crawler might have been turned off for a while
     * Then the average time is added to the total time.
     */
    public void updateCrawlTime() {
        long lastInterval = (System.currentTimeMillis() - lastCrawled);
        if(totalCrawls < ContinuousController.BURNIN_CRAWLS) {
            totalTime += lastInterval;
        }
        else {
            if(lastInterval > (averageCrawl() * 2)) {
                totalTime += averageCrawl();
            }
            else {
                totalTime += lastInterval;
            }
        }
        lastCrawled = System.currentTimeMillis();
    }

    public long averageCrawl() {
        return totalTime / totalCrawls;
    }

}

