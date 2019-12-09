package uk.ac.susx.tag.norconex.jobqueuemanager;

// jqm imports
import com.beust.jcommander.JCommander;
import com.enioka.jqm.api.JobRequest;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// queue manager imports
import uk.ac.casm.jqm.manager.CLIArguments;
import uk.ac.casm.jqm.manager.IndependentPollingManager;

// java imports
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class CrawlerPollingManager extends IndependentPollingManager {

    protected static final Logger logger = LoggerFactory.getLogger(CrawlerPollingManager.class);

    // Properties file read/constructed by submission service
    public CrawlerPollingManager(Properties properties, boolean restart) {
        super(properties, restart);
    }

    @Override
    public JobRequest createJobRequest(JobRequest jobRequest) {
        jobRequest.addParameter(SingleSeedCollector.SEED,jobRequest.getKeyword1());
        return jobRequest;
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
        return CrawlerSubmissionService.CRAWLERJOB;
    }

    @Override
    public String getUserName() { return "crawler-manager";}

    @Override
    public void restart() {
        // Not needed for this implementation.
    }

    @Override
    public void report() {
        // Not needed for this implementation.
    }

    public static void main(String[] args){

        List<String> splitArgs = new ArrayList<>();
        for(String arg : args) {
            splitArgs.addAll(Arrays.asList(arg.split("\\s+")));
        }

        CLIArguments cli = new CLIArguments();
        new JCommander().newBuilder()
                .addObject(cli)
                .build()
                .parse(splitArgs.toArray(new String[splitArgs.size()]));

        Properties props = new Properties();
        try(BufferedReader reader = new BufferedReader(new FileReader(cli.properties))) {
            props.load(reader);
        } catch (FileNotFoundException e) {
            new RuntimeException("Failed when attempting to load props file at: " + cli.properties);
        } catch (IOException e) {
            new RuntimeException("Failed when attempting to load props file at: " + cli.properties);
        }


        CrawlerPollingManager cpm = new CrawlerPollingManager(props,false);

        if(cli.kill){
            cpm.killAll();
        } else {
            cpm.start(Long.valueOf(props.getProperty(IndependentPollingManager.POLLTIME)));
        }

    }

}
