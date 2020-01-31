package uk.ac.susx.tag.norconex.crawlpolling;

// jqm imports

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.State;
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
import java.util.concurrent.*;

// json imports
// logging imports
// java imports

/**
 * Polls jobs with a given application name and restarts them if they have completed.
 * Can be configured to remove jobs from the register of they have failed.
 */
public abstract class IndependentPollingManager {

    protected static final Logger logger = LoggerFactory.getLogger(QueuedPollingManager.class);

    // Default time delay in (2hrs) in seconds between checking on jobs and queue(s) -
    private static final long POLLWAIT = 7200;

    // Custom properties
    public static final String CACHE = "casm.jqm.polling.cache";       // place to store running/queued job cache
    public static final String JOBRESTART = "casm.jqm.polling.job.restart";  // Specify whether failed jobs should be restarted
    public static final String PROPS = "casm.jqm.polling.props";
    public static final String POLLTIME = "casm.jqm.polling.time";

    protected final Properties properties;                             // manager and
    private final boolean jobRestart;
    private final Path cacheLocation;

    // Registry of keys and ids for jobs requiring monitoring and restarting
    protected Map<String,Integer> register;

    // Used to add next scheduled queue/job check
    private final ScheduledExecutorService scheduler;

    private boolean finished;   // Can be used by implementing classes to signal shutdown

    /**
     * Standard constructor but with restart parameter - if true will check for previous job cache and restart monitor
     * @param properties
     * @param restart
     */
    public IndependentPollingManager(Properties properties, boolean restart) {

        scheduler = Executors.newScheduledThreadPool(10);

        this.properties = properties;

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

            throw new RuntimeException("Unable to save job cache: " + e.getMessage() + " " + cacheLocation.toString());
        }

    }

    /**
     * @return returns true if this manager has been instructed to shutdown
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Starts the polling manager using the default wait period (in seconds)
     */
    public void start() {
        start(POLLWAIT);
    }

    /**
     * Starts a job check scheduled to poll regularly with the specified duration.
     * @param waitSeconds
     */
    public void start(long waitSeconds) {
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(new JobPolling(), waitSeconds, waitSeconds, TimeUnit.SECONDS);
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        logger.info("INFO: Started scheduler at rate of every " + waitSeconds + " seconds.");
    }


    /**
     * Sends the shutdown signal to the scheulded poll manager
     * Waits for all crawlers to finish.
     */
    public void stop() throws InterruptedException {
        finished = true;
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
        return JqmClientFactory.getClient().enqueue(job);
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
                register.put((String) key, Math.toIntExact(Long.valueOf((Long) json.get(key))));
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
        JqmClientFactory.setProperties(properties);
        Map<String,Integer> relevantJobs = (register == null) ? new HashMap<>() : register;

        for(JobInstance job : JqmClientFactory.getClient().getActiveJobs()) {
            if(job.getApplicationName().equals(appName)) {
                relevantJobs.put(job.getKeyword1(),job.getId());
            }
        }
        logger.info("INFO: " + relevantJobs.size() + " crawling jobs found on the queue or running");
        return relevantJobs;
    }

    /**
     * Will kill all jobs relevant to this polling manager
     */
    public void killAll() {
        for (JobInstance job : JqmClientFactory.getClient().getJobs()) {
            if(job.getApplicationName().equals(getAppName())) {
                JqmClientFactory.getClient().deleteJob(job.getId());
                JqmClientFactory.getClient().cancelJob(job.getId());
                JqmClientFactory.getClient().killJob(job.getId());
            }
        }
    }

    /**
     * Runnable class which polls the queue and restarts any jobs which have finished or failed (if configured to allow failed job restart)
     */
    public class JobPolling implements Runnable {

        @Override
        public void run() {

            logger.info("INFO: Polling queue for crawler jobs.");

            // Update the register to check for new jobs
            getRelevantJobs();

            for(Map.Entry<String,Integer> job : register.entrySet()) {

                JobInstance ji = JqmClientFactory.getClient().getJob(job.getValue());
                State state = ji.getState();
                switch (state) {

                    case CRASHED:
                        logger.warn("WARN: The job with id: " + job.getValue() + " and name: " + job.getKey() + " has failed");
                        if(jobRestart) {
                            logger.warn("WARN: Trying to restart failed job for seed " + ji.getKeyword1() + ".");
                            JobRequest newJob = createJobRequest(job.getKey());
                            newJob.setParameters(ji.getParameters());
                            int id = postJobRequest(newJob);
                            register.put(job.getKey(),id);
                        } else {
                            // remove the job from the registry so it is not checked or restarted
                            deRegisterJob(job.getValue().intValue(),job.getKey(),true);
                        }
                        break;

                    case KILLED:
                        logger.info("INFO: Crawler job for seed: " + ji.getKeyword1() + " id: " + ji.getId() + " has been killed.");
                        deRegisterJob(job.getValue().intValue(),job.getKey(),true);
                        break;

                    // Scheduler needs to do nothing in these cases.
                    case RUNNING:
                        logger.info("INFO: Crawler job for seed: " + ji.getKeyword1() + " id: " + ji.getId() + " is running.");
                        break;

                    case SCHEDULED:
                        logger.info("INFO: Crawler job for seed: " + ji.getKeyword1() + " id: " + ji.getId() + " is scheduled to run.");
                        break;

                    case SUBMITTED:
                        logger.info("INFO: Crawler job for seed: " + ji.getKeyword1() + " id: " + ji.getId() + " has been submitted and is waiting to run.");
                        break;

                    case ENDED:
                        logger.info("INFO: Crawler job for seed: " + ji.getKeyword1() + " id: " + ji.getId() + " has completed successfully. Restarting now.");
                        JobRequest newJob = createJobRequest(job.getKey());
                        newJob.setParameters(ji.getParameters());
                        for(Map.Entry<String,String> param : ji.getParameters().entrySet()) {
                            logger.warn("WARN: " + param.getKey() + " - " + param.getValue());
                        }
                        int id = JqmClientFactory.getClient().enqueue(newJob);
                        register.put(job.getKey(),id);
                        break;

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

    // convenience method to implement if reporting is required (needed?)
    public abstract void report();

    // Can either be instantiated to retrieve the cached register or implement your own
    public abstract Map<String,Integer> getRegister(Path registerLocation);

    // App name required to differentiate between jobs on the queue and the relevant manager instance
    public abstract String getAppName();

    // Username of the caller, not important for this api but used by JQM
    public abstract String getUserName();

}
