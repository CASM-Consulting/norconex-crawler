package uk.ac.susx.tag.norconex.collector;

import com.norconex.collector.http.crawler.HttpCrawler;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.jef4.status.JobStatusUpdater;
import com.norconex.jef4.status.MutableJobStatus;
import com.norconex.jef4.suite.JobSuite;

public class ContinuousCrawler extends HttpCrawler {
    /**
     * Constructor.
     *
     * @param crawlerConfig HTTP crawler configuration
     */
    public ContinuousCrawler(HttpCrawlerConfig crawlerConfig) {
        super(crawlerConfig);
    }

}
