package uk.ac.susx.tag.norconex.jobqueuemanager;

import com.enioka.jqm.api.JobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.crawlpolling.SubmissionService;
import uk.ac.susx.tag.norconex.utils.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * Submission service for running single seed crawlers that pump data straight into a configured database
 */
public class DBTableSubmissionService extends SubmissionService {

    protected static final Logger logger = LoggerFactory.getLogger(DBTableSubmissionService.class);

    public static final String CRAWLERJOB = "Straight2DBCollector";
    public static final String CRAWLERUSER = "straight-to-db-submitter";
    public static final String QUEUE = "DBCrawlers";

    public DBTableSubmissionService(Properties props) {
        super(props);
    }

    @Override
    public void submitSeed(Map<String, String> seed, String jobdef) {

        JobRequest jobReq = new JobRequest(CRAWLERJOB,CRAWLERUSER);
        jobReq.setQueueName(QUEUE);
        jobReq.addParameter(SingleSeedCollector.SEED, SingleSeedCollector.SEED + " " + seed.get(LINK));

        try {
            jobReq.addParameter(SingleSeedCollector.ID, SingleSeedCollector.ID + " " + seed.get(Utils.getDomain(seed.get(LINK))));
            jobReq.setKeyword1(seed.get(SOURCE));
        } catch (URISyntaxException e) {
            logger.error("Invalid URL: " + seed.get(LINK) + " " + e.getMessage());
        }

        this.submitJobRequest(jobReq);

    }

    public static void main(String[] args) {
        Path links = Paths.get(args[0]);
        Properties props = Utils.getProperties(args[1]);
        DBTableSubmissionService css = new DBTableSubmissionService(props);
        try {
            css.submitSeeds(DBTableSubmissionService.loadSeeds(links), CRAWLERJOB);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}