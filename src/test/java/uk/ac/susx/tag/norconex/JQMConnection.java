package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

public class JQMConnection {

    @Test
    public void testJQMConnection() {
        Properties props = new Properties();
        props.put("com.enioka.jqm.ws.url", "http://localhost:56379/ws/client");
        JqmClientFactory.setProperties(props);
        List<JobInstance> jobs = JqmClientFactory.getClient().getJobs();
        Assert.assertTrue("Test failed when attempting to connect to service",jobs.size() >= 0);
    }

}
