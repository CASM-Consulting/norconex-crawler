package uk.ac.susx.tag.norconex.jobqueuemanager;

// jqm imports
import com.beust.jcommander.JCommander;
import com.enioka.jqm.api.JobRequest;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// queue manager imports
import uk.ac.casm.jqm.manager.PollingManager;

// java imports
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CrawlerPollingManager extends PollingManager {

    protected static final Logger logger = LoggerFactory.getLogger(CrawlerPollingManager.class);

//    private final CrawlerFactory factory;

    // Properties file read/ constructed by submission service
    // crawler needs to define the properties for this
    public CrawlerPollingManager(Properties properties, boolean restart) {
        super(properties, restart);
    }

    @Override
    public JobRequest createJobRequest(JobRequest jobRequest) {
        jobRequest.addParameter(SingleSeedCollector.SEED,jobRequest.getKeyword1());
        return jobRequest;
    }

    @Override
    public Map<String,String> buildParameters() {
        Map<String,String> enqueueParams = new HashMap<>();
        enqueueParams.put(SingleSeedCollector.USERAGENT,properties.getProperty(SingleSeedCollector.USERAGENT));
        enqueueParams.put(SingleSeedCollector.CRAWLB, properties.getProperty(SingleSeedCollector.CRAWLB));
        enqueueParams.put(SingleSeedCollector.DEPTH,properties.getProperty(SingleSeedCollector.DEPTH));
        enqueueParams.put(SingleSeedCollector.POLITENESS,properties.getProperty(SingleSeedCollector.POLITENESS));
        enqueueParams.put(SingleSeedCollector.SITEMAP, properties.getProperty(SingleSeedCollector.SITEMAP));
        enqueueParams.put(SingleSeedCollector.ROBOTS,properties.getProperty(SingleSeedCollector.ROBOTS));
        enqueueParams.put(SingleSeedCollector.ID, properties.getProperty(SingleSeedCollector.ID));
        enqueueParams.put(SingleSeedCollector.FILTER,properties.getProperty(SingleSeedCollector.FILTER));
        enqueueParams.put(SingleSeedCollector.THREADS,properties.getProperty(SingleSeedCollector.THREADS));
        return enqueueParams;
    }

    @Override
    public Map<String, Integer> getRegister(Path path) {
        try {
            return loadJobQueue(path);
        } catch (Exception e) {
            logger.error("ERROR: Failed to load cache at given location");
            throw new RuntimeException("ERROR: Failed to load cache at given location");
        }
    }

    @Override
    public String getAppName() {
        return "incremental-crawling";
    }

    @Override
    public String userName() {
        return "crawler-manager";
    }

    @Override
    public String createKeyword() {
        // Not needed for this implementation
        return null;
    }

    @Override
    public void restart() { // Not needed for this implementation.
    }

    @Override
    public void report() { // Not needed for this implementation.
    }

    public static void main(String[] args){

        CLIArguments cli = new CLIArguments();
        new JCommander().newBuilder()
                .addObject(cli)
                .build()
                .parse(args);

        Properties props = new Properties();
        try(BufferedReader reader = new BufferedReader(new FileReader(cli.properties))) {
            props.load(reader);
        } catch (FileNotFoundException e) {
            new RuntimeException("Failed when attempting to load props file at: " + cli.properties);
        } catch (IOException e) {
            new RuntimeException("Failed when attempting to load props file at: " + cli.properties);
        }
        props.put(CACHE,cli.cache);

        CrawlerPollingManager cpm = new CrawlerPollingManager(props,cli.restart);
        cpm.start();


    }
}
