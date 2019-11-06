package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobRequest;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.casm.jqm.manager.SubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.SingleSeedCollector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PollingManagerSubmissionTest {



    @Test
    public void pollingManagerSubmissionTest() {


        String propsLoc = "tests/sumbission-props";


        Properties props = new Properties();
        props.put("com.enioka.jqm.ws.url", "http://localhost:56379/ws/client");

        try(BufferedReader reader = new BufferedReader(new FileReader(propsLoc))) {
            props.load(reader);
        } catch (FileNotFoundException e) {
            Assert.fail();
        } catch (IOException e) {
            Assert.fail();
        }
        SubmissionService ss = new SubmissionService(props);
        JobRequest jobRequest = JobRequest.create("CrawlerManager","jp242");

        String seed = "http://www.taglaboratory.org/";



        Assert.assertTrue("Job submission test failed", Integer.valueOf(ss.submitJobRequest(jobRequest)) instanceof Integer);


    }
}
