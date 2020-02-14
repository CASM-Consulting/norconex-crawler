package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobRequest;
//import com.enioka.jqm.api.JqmClientFactory;

import com.enioka.jqm.api.JqmClientFactory;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.tag.norconex.crawlpolling.IndependentPollingManager;
import uk.ac.susx.tag.norconex.crawlpolling.SubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerPollingManager;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerSubmissionService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PollingManagerSubmissionTest {

    @Test
    public void pollingManagerSubmissionTest() {

        String propsLoc = "/Users/jp242/Documents/Projects/JQM-Crawling/jqm_root/conf/crawlmanager.properties";

        Properties props = new Properties();
        props.put("com.enioka.jqm.ws.url", "http://jqm....:36792/ws/client");

        try(BufferedReader reader = new BufferedReader(new FileReader(propsLoc))) {
            props.load(reader);
        } catch (FileNotFoundException e) {
            Assert.fail();
        } catch (IOException e) {
            Assert.fail();
        }
        SubmissionService ss = new CrawlerSubmissionService(props);
        JobRequest jobRequest = JobRequest.create("CrawlerManager","jp242");
        JqmClientFactory.setProperties(props);


        jobRequest.addParameter(IndependentPollingManager.CACHE, IndependentPollingManager.CACHE + " " + props.getProperty(IndependentPollingManager.CACHE));
        jobRequest.addParameter(IndependentPollingManager.PROPS, IndependentPollingManager.PROPS + " " + propsLoc);

//        jobRequest.addParameter(IndependentPollingManager.JOBRESTART, IndependentPollingManager.JOBRESTART + "")
//        jobRequest.addParameter(IndependentPollingManager.)

        JobRequest crawler = TestCrawlerSubmission.createJobRequest();

//        JqmClientFactory.getClient().enqueue(jobRequest);
//        ss.submitJobRequest(jobRequest);
        ss.submitJobRequest(crawler);

        CrawlerPollingManager cpm = new CrawlerPollingManager(props,true);
        cpm.start(10);
//

//        Assert.assertTrue("Job submission test failed", Integer.valueOf(ss.submitJobRequest(jobRequest)) instanceof Integer);


    }
}
