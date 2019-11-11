package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class JQMDemoSubmissionTest {


    @Test
    public void testDemoJQMEnqueue() {

        Properties props = new Properties();
        props.put("com.enioka.jqm.ws.url", "http://localhost:49910/ws/client");

        JobRequest jobRequest = JobRequest.create("DemoFibo1","jp242");
        JqmClientFactory.setProperties(props);

        Assert.assertTrue("Failed on submission", JqmClientFactory.getClient().enqueue(jobRequest) >= 0);
    }

}
