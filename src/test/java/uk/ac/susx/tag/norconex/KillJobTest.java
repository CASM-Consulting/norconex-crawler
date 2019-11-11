package uk.ac.susx.tag.norconex;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.casm.jqm.manager.SubmissionService;

import java.util.Properties;

public class KillJobTest {

    @Test
    public void killJob() {

        int jobId = 1357;
        Properties props = new Properties();
        props.put("com.enioka.jqm.ws.url", "http://localhost:49910/ws/client");
        JqmClientFactory.setProperties(props);
        JqmClientFactory.getClient().killJob(jobId);

    }

}
