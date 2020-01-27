package uk.ac.susx.tag.norconex.crawlpolling;

// jqm imports

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// json imports
// logging imports
// java imports

/**
 * Sits as a job on the queue and polls jobs with a given application name and restarts them if they have completed.
 * Can be configured to remove jobs from the register of they have failed.
 */
public abstract class QueuedPollingManager {

    protected static final Logger logger = LoggerFactory.getLogger(QueuedPollingManager.class);

    // Time delay between checking on jobs and queue(s)
    private static final long POLLWAIT = 7200;

    // Custom properties
    public static final String CACHE = "casm.jqm.polling.cache";             // place to store running/queued job cache
    public static final String RESTART = "casm.jqm.polling.job.restart";  // Specify whether failed jobs should be restarted

    public static final String PROPS = "casm.jqm.polling.props";
    public static final String KILLALL = "casm.jqm.polling.kill";


    protected Properties properties;                             // manager and
    private final boolean jobRestart;
    private final Path cacheLocation;
    protected JobManager jobManager;                                   // used to get other jobs statuses

    // Registry of keys and ids for jobs requiring monitoring and restarting
    protected Map<String,Integer> register;

    // Used to add next scheduled queue/job check
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private boolean finished;   // Can be used by implementing classes to signal shutdown

    /**
     * Standard constructor but with restart parameter - if true will check for previous job cache and restart monitor
     * @param properties
     * @param restart
     */
    public QueuedPollingManager(Properties properties, boolean restart) {

        this.properties = properties;
//        JqmClientFactory.setProperties(properties);

        jobRestart = false;
        finished = false;
        if(restart) {
            restart(Paths.get(properties.getProperty(CACHE)));
        }
        register = getRelevantJobs();
        cacheLocation = Paths.get(properties.getProperty(CACHE));
        try {
            saveJobQueue(cacheLocation);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save job cache");
        }

    }

    public QueuedPollingManager(boolean restart) {
        this(null,restart);
    }

    /**
     * @return returns true if this manager has been instructed to shutdown
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Starts the polling manager using the default wait period
     */
    public void start() {
        start(POLLWAIT);
    }

    /**
     * Starts a job check scheduled to poll regularly with the specified duration.
     * @param waitSeconds
     */
    public void start(long waitSeconds) {
        scheduler.scheduleAtFixedRate(new JobPolling(),0, waitSeconds, TimeUnit.SECONDS);
    }


    /**
     * Sends the shutdown signal to the scheulded poll manager
     * Waits for all crawlers to finish.
     */
    public void stop() throws InterruptedException {
        finished = true;
        jobManager.yield();
        scheduler.shutdown();
        scheduler.awaitTermination(1000, TimeUnit.HOURS);
    }

    /**
     * Shuts downthe poll manager and kills all jobs instantly.
     */
    public void stopNow() {
        finished = true;
        for(Integer id : register.values()) {
            JqmClientFactory.getClient().killJob(id);
            JqmClientFactory.getClient().cancelJob(id);
        }
        scheduler.shutdownNow();
        jobManager.yield();
    }

    /**
     * Remove a job from the monitor registry and potentially kill it
     * @param id
     * @param key
     * @param killJob
     */
    public void deRegisterJob(int id, String key, boolean killJob) {
        if(register.containsValue(id)){
            register.remove(key);
        } else {
          if(register.containsKey(key)) {
              register.remove(key);
          }
        }
        if(killJob) {
            JqmClientFactory.getClient().killJob(id);
        }
    }

    // create a new an application specific job request to add to the queue
    public JobRequest createJobRequest(String key){
        JobRequest job = JobRequest.create(getAppName(), getUserName());
        job.setKeyword1(key);
        return createJobRequest(job);
    }

    /**
     * Check if a job with the given id or keyword is currently running, and if not submits the job.
     * @param i
     * @param job
     * @return
     */
    public int postJobRequest(int i, JobRequest job) {
        List<JobInstance> jobs = JqmClientFactory.getClient().getActiveJobs();
        for(JobInstance jobinstance : jobs) {
            if(jobinstance.getId().intValue() == i || jobinstance.getKeyword1().equals(job.getKeyword1())) {
                return -1;
            }
        }
        return postJobRequest(job);
    }


    /**
     * Simply submits a job without performing any checks first
     * @param job
     * @return
     */
    public int postJobRequest(JobRequest job) {
        JobRequest jr = JobRequest.create(getAppName(),getUserName());
        jr.addParameters(buildParameters());
        return JqmClientFactory.getClient().enqueue(jr);
    }


    /**
     * Restart the manager from the cached job ids.
     * @param jobIdPath
     */
    public void restart(Path jobIdPath) {
        try {
            register = loadJobQueue(jobIdPath);
            restart();
        } catch (IOException e) {
            throw new RuntimeException("Error when parsing the seed/jobid map.");
        } catch (ParseException e) {
            throw new RuntimeException("Error when parsing the seed/jobid map.");
        }
    }

    /**
     * Saves/caches the current jobs to be monitored
     * @param jobIdPath
     * @throws IOException
     */
    public void saveJobQueue(Path jobIdPath) throws IOException {

        JSONObject output = new JSONObject();
        for(Map.Entry<String,Integer> entry : register.entrySet()) {
            output.put(String.valueOf(entry.getKey()),entry.getValue());
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(jobIdPath.toFile()))){
            bw.write(output.toString());
        }

    }

    /**
     * Loads the current jobs to be monitored.
     * @param jobIdPath
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public HashMap<String,Integer> loadJobQueue(Path jobIdPath) throws IOException, ParseException {

        try(BufferedReader br = new BufferedReader(new FileReader(jobIdPath.toFile()))) {
            JSONParser jp = new JSONParser();
            JSONObject json =  (JSONObject) jp.parse(br);
            HashMap<String,Integer> register = new HashMap<>();
            for(Object key : json.keySet()) {
                register.put((String) key, Integer.valueOf((Integer) json.get(key)));
            }
            return register;
        }

    }

    /**
     * Uses the concrete class specified application name to discover all jobs on the queue relevant to that application (also sharing the same name)
     * @return Map of all relevant jobs to be monitored by the application
     */
    public Map<String, Integer> getRelevantJobs() {
        String appName = getAppName();
        Map<String,Integer> relevantJobs = (register == null) ? new HashMap<>() : register;
        for(JobInstance job : JqmClientFactory.getClient().getActiveJobs()) {
            if(job.getApplicationName().equals(appName)) {
                relevantJobs.put(job.getKeyword1(),job.getId());
            }
        }
        return relevantJobs;
    }

    /**
     * Runnable class which polls the queue and restarts any jobs which have finished or failed (if configured to allow failed job restart)
     */
    public class JobPolling implements Runnable {

        @Override
        public void run() {

            // Update the register to check for new jobs
            getRelevantJobs();

            for(Map.Entry<String,Integer> job : register.entrySet()) {

                // If it has job started or has not finished continue
                if(!jobManager.hasEnded(job.getValue().intValue())) {
                    continue;
                }
                // If it has failed log - and only restart if configured to do so
                if(jobManager.hasFailed(job.getValue().intValue())) {
                    logger.warn("WARN: The job with id: " + job.getValue() + " and name: " + job.getKey() + " has failed");
                    if(jobRestart) {
                        JobRequest newJob = createJobRequest(job.getKey());
                        int id = postJobRequest(newJob);
                        register.put(job.getKey(),id);
                    } else {
                        // remove the job from the registry so it is not checked or restarted
                        deRegisterJob(job.getValue().intValue(),job.getKey(),true);
                    }
                }

                // If it has succeeded then restart
                if(jobManager.hasSucceeded(job.getValue().intValue())) {
                    JobRequest newJob = createJobRequest(job.getKey());
                    int id = postJobRequest(newJob);
                    register.put(job.getKey(),id);
                }

            }

            // Cache the queue in case of failure or need of restart
            try {
                saveJobQueue(cacheLocation);
            } catch (IOException e) {
                throw new RuntimeException("Failed when attempting to save job cache.");
            }
        }
    }

    // empty job request to inject paramaters and functionality (best to use a factory)
    public abstract JobRequest createJobRequest(JobRequest emptyJob);

    // called when class restart is called - convenience method to inject own functionality on restart
    public abstract void restart();

    // builds the jqm paramaeters needed to communicate with the queue
    public abstract Map<String,String> buildParameters();

    // convenience method to implement if reporting is required (needed?)
    public abstract void report();

    // Can either be instantiated to retrieve the cached register or implement your own
    public abstract Map<String,Integer> getRegister(Path registerLocation);

//    // Keyword is required to act as a String id for a job (e.g. a crawler may use the domain it is crawling as an identifier)
    public abstract String createKeyword();

    // App name required to differentiate between jobs on the queue and the relevant manager instance
    public abstract String getAppName();

    // Username of the caller, not important for this api but used by JQM
    public abstract String getUserName();


}
