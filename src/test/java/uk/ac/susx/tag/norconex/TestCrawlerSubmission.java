package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobRequest;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.casm.jqm.manager.SubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.SingleSeedCollector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class TestCrawlerSubmission {

    @Test
    public void testSubmitCrawler() {

        Properties props = new Properties();
        props.put("com.enioka.jqm.ws.url", "http://localhost:49910/ws/client");

        SubmissionService ss = new SubmissionService(props);


//        jobRequest.addParameter(SingleSeedCollector.ROBOTS, SingleSeedCollector.ROBOTS);
//
//        jobRequest.addParameter(SingleSeedCollector.SITEMAP, SingleSeedCollector.SITEMAP);

        Assert.assertTrue("Job submission test failed", Integer.valueOf(ss.submitJobRequest(createJobRequest())) instanceof Integer);

    }

    public static JobRequest createJobRequest(){

        JobRequest jobRequest = JobRequest.create("CrawlerDef","jp242");

        String seed = "https://www.ebay.co.uk/";
        jobRequest.setKeyword1(seed);

        String domain = "test-host";
        try {
            URL url = new URL(seed);
            domain = url.getHost();
        } catch (MalformedURLException e) {
            Assert.fail();
        }


        jobRequest.addParameter(SingleSeedCollector.SEED, SingleSeedCollector.SEED + " " + seed);

        jobRequest.addParameter(SingleSeedCollector.CRAWLB, SingleSeedCollector.CRAWLB + " " + "tests/crawldb");

        jobRequest.addParameter(SingleSeedCollector.DEPTH, SingleSeedCollector.DEPTH + " " + "1");

        jobRequest.addParameter(SingleSeedCollector.POLITENESS, SingleSeedCollector.POLITENESS + " " + "500");

        jobRequest.addParameter(SingleSeedCollector.ID, SingleSeedCollector.ID + " " + domain);

        jobRequest.addParameter(SingleSeedCollector.THREADS, SingleSeedCollector.THREADS + " " + "2");

        jobRequest.addParameter(SingleSeedCollector.USERAGENT, SingleSeedCollector.USERAGENT + " " + "m52");

        return jobRequest;

    }

}
