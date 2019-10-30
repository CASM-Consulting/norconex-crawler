package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobRequest;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.casm.jqm.manager.SubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.SingleSeedCollector;

import java.util.Properties;

public class TestCrawlerSubmission {

    @Test
    public void testSubmitCrawler() {

        Properties props = new Properties();
        props.put("com.enioka.jqm.ws.url", "http://localhost:56379/ws/client");

        SubmissionService ss = new SubmissionService(props);
        JobRequest jobRequest = JobRequest.create("CrawlerDef","jp242");

        String seed = "http://www.taglaboratory.org/";

        jobRequest.addParameter(SingleSeedCollector.SEED,seed);
        jobRequest.addParameter(SingleSeedCollector.CRAWLB,"tests/crawldb");
        jobRequest.addParameter(SingleSeedCollector.DEPTH,"0");
        jobRequest.addParameter(SingleSeedCollector.POLITENESS,"3000");
        jobRequest.addParameter(SingleSeedCollector.ID,"test-incremental");
        jobRequest.addParameter(SingleSeedCollector.THREADS,"2");
        jobRequest.addParameter(SingleSeedCollector.USERAGENT,"m52");
        jobRequest.addParameter(SingleSeedCollector.ROBOTS,"true");
        jobRequest.addParameter(SingleSeedCollector.SITEMAP,"true");

        Assert.assertTrue("Job submission test failed", Integer.valueOf(ss.submitJobRequest(jobRequest)) instanceof Integer);

    }

}
