package uk.ac.susx.tag.norconex.crawlpolling;

// jqm imports

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import uk.ac.susx.tag.norconex.utils.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

// java imports

/**
 * Intended to be used as a service for configuring and submitting jobs enmasse
 */
public class SubmissionService {

    private final Properties properties;
    private JobInstance manager;

    public SubmissionService() {
        this(new Properties());
    }

    public SubmissionService(Properties properties) {
        this.properties = properties;
    }

    public SubmissionService(String propertiesLocation) {
        this(Utils.getProperties(propertiesLocation));
    }

    public SubmissionService(Path propertiesLocation) {
        properties = Utils.getProperties(propertiesLocation);
    }

    public void setManager(int jobId) {
        manager = JqmClientFactory.getClient().getJob(jobId);
    }

    public int submitJobRequest(JobRequest request) {

        JqmClientFactory.setProperties(properties);         // reiterated each time to ensure it remains up-to-date
        return JqmClientFactory.getClient().enqueue(request);

    }

    public static int submitJobRequest(JobRequest request, Properties properties) {

        JqmClientFactory.setProperties(properties);
        return JqmClientFactory.getClient().enqueue(request);

    }

}
