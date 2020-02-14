package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobRequest;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.tag.norconex.crawlpolling.SubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerArguments;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerSubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.SingleSeedCollector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class TestCrawlerSubmission {

    @Test
    public void testSubmitCrawler() {

        Properties props = new Properties();
        props.put("com.enioka.jqm.ws.url", "http://localhost:49910/ws/client");
//        props.put("com.enioka.jqm.ws.url", "https://jqm.casmconsulting.co.uk/ws/client");

        SubmissionService ss = new CrawlerSubmissionService(props);

        Assert.assertTrue("Job submission test failed", Integer.valueOf(ss.submitJobRequest(createJobRequest())) instanceof Integer);

    }

    public static JobRequest createJobRequest(){

        JobRequest jobRequest = JobRequest.create("SpringCollector","jp242");

        String seed = "http://www.arabtimesonline.com/news/assets-low-debt-sound-banks-prop-kuwait-resilience/";
        String sourceDomain = "http://www.arabtimesonline.com/news/";
        String sourceName = "Arab_Times";
        jobRequest.setKeyword1(sourceDomain);

        String domain = "test-host";
        try {
            URL url = new URL(seed);
            domain = url.getHost();
        } catch (MalformedURLException e) {
            Assert.fail();
        }


        jobRequest.addParameter(SingleSeedCollector.SEED, SingleSeedCollector.SEED + " " + seed);

//        jobRequest.addParameter(SingleSeedCollector.CRAWLB, SingleSeedCollector.CRAWLB + " " + "/Users/jp242/Documents/Projects/JQM-Crawling/crawl-databases");

        jobRequest.addParameter(SingleSeedCollector.DEPTH, SingleSeedCollector.DEPTH + " " + "0");

        jobRequest.addParameter(SingleSeedCollector.POLITENESS, SingleSeedCollector.POLITENESS + " " + "450");

        jobRequest.addParameter(SingleSeedCollector.ID, SingleSeedCollector.ID + " " + domain);

        jobRequest.addParameter(SingleSeedCollector.THREADS, SingleSeedCollector.THREADS + " " + "1");

        jobRequest.addParameter(SingleSeedCollector.USERAGENT, SingleSeedCollector.USERAGENT + " " + "CASM");

        jobRequest.addParameter(SingleSeedCollector.SITEMAP,SingleSeedCollector.SITEMAP + " false");

        jobRequest.addParameter(SingleSeedCollector.ROBOTS,SingleSeedCollector.ROBOTS + " false");

//        jobRequest.addParameter(SingleSeedCollector.INDEXONLY, SingleSeedCollector.INDEXONLY);

        jobRequest.addParameter(CrawlerArguments.SCRAPER, CrawlerArguments.SCRAPER + " " + "arabtimesonline.json");

        jobRequest.addParameter(CrawlerArguments.SCRAPERS, CrawlerArguments.SCRAPERS + " " + "/Users/jp242/Documents/Projects/JQM-Crawling/example_scrapers");

        jobRequest.addParameter(CrawlerArguments.SOURCEDOMAIN, CrawlerArguments.SOURCEDOMAIN + " " + sourceDomain);

        jobRequest.addParameter(CrawlerArguments.SOURCENAME, CrawlerArguments.SOURCENAME + " " + sourceName);

        // configure spring to use the local properties file (i.e. not to use pg-bouncer)
        jobRequest.addParameter(CrawlerArguments.LOCALSPRINGPROPS, CrawlerArguments.LOCALSPRINGPROPS);

        return jobRequest;

    }

}
