package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobRequest;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.tag.norconex.crawlpolling.SubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerArguments;
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

        SubmissionService ss = new SubmissionService(props);


//        jobRequest.addParameter(SingleSeedCollector.ROBOTS, SingleSeedCollector.ROBOTS);
//
//        jobRequest.addParameter(SingleSeedCollector.SITEMAP, SingleSeedCollector.SITEMAP);

        Assert.assertTrue("Job submission test failed", Integer.valueOf(ss.submitJobRequest(createJobRequest())) instanceof Integer);

    }

    public static JobRequest createJobRequest(){

        JobRequest jobRequest = JobRequest.create("SpringCollector","jp242");

        String seed = "http://www.arabtimesonline.com/news/";
        jobRequest.setKeyword1(seed);

        String domain = "test-host";
        try {
            URL url = new URL(seed);
            domain = url.getHost();
        } catch (MalformedURLException e) {
            Assert.fail();
        }


        jobRequest.addParameter(SingleSeedCollector.SEED, SingleSeedCollector.SEED + " " + seed);

//        jobRequest.addParameter(SingleSeedCollector.CRAWLB, SingleSeedCollector.CRAWLB + " " + "/Users/jp242/Documents/Projects/JQM-Crawling/crawl-databases");

        jobRequest.addParameter(SingleSeedCollector.DEPTH, SingleSeedCollector.DEPTH + " " + "4");

        jobRequest.addParameter(SingleSeedCollector.POLITENESS, SingleSeedCollector.POLITENESS + " " + "350");

        jobRequest.addParameter(SingleSeedCollector.ID, SingleSeedCollector.ID + " " + domain);

        jobRequest.addParameter(SingleSeedCollector.THREADS, SingleSeedCollector.THREADS + " " + "2");

        jobRequest.addParameter(SingleSeedCollector.USERAGENT, SingleSeedCollector.USERAGENT + " " + "CASM");

        jobRequest.addParameter(SingleSeedCollector.SITEMAP,SingleSeedCollector.SITEMAP + " false");

        jobRequest.addParameter(SingleSeedCollector.ROBOTS,SingleSeedCollector.ROBOTS + " false");

//        jobRequest.addParameter(SingleSeedCollector.INDEXONLY, SingleSeedCollector.INDEXONLY);

        jobRequest.addParameter(CrawlerArguments.SCRAPER, CrawlerArguments.SCRAPER + " " + "arabtimesonline.json");

        jobRequest.addParameter(CrawlerArguments.SCRAPERS, CrawlerArguments.SCRAPERS + " " + "/Users/jp242/Documents/Projects/JQM-Crawling/example_scrapers");

        return jobRequest;

    }

}
