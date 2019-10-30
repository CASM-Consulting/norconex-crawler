//package uk.ac.susx.tag.norconex.jobqueuemanager;
//
//// jqm imports
//import com.enioka.jqm.api.*;
//
//// json imports
//import com.uwyn.jhighlight.fastutil.Hash;
//import org.json.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import uk.ac.susx.tag.norconex.controller.ContinuousController;
//
//// java imports
//import java.io.*;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///**
// * The job queue management class for monitoring and persiting continuous crawlers.
// */
//public class JQManagement {
//
//    protected static final Logger logger = LoggerFactory.getLogger(ContinuousController.class);
//
//    private static final long POLLWAIT = 7200; // seconds - time to wait before next poll (2hrs)
//    private static final String FAILED = "Crawl failed!";
//    private static Map<String,Integer> idSeeds = new HashMap<>();
//
//    private static String server;
//
//    private CrawlerArguments arguments;
//    private JobManager jobManager;
//    private boolean finished;
//    private Path cachePath;
//
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//
//    public JQManagement(CrawlerArguments ca, String serverUrl) {
//        this(ca);
//        server = serverUrl;
//        Properties props = new Properties();
//        props.put("com.enioka.jqm.ws.url",server);
//        System.out.println(props.getProperty("com.enioka.jqm.ws.url"));
//        props.put("com.enioka.jqm.ws.login","jp242");
//        props.put("com.enioka.jqm.ws.password","ekfh7fv8");
//        JqmClientFactory.setProperties(props);
//    }
//
//    public JQManagement(CrawlerArguments ca) {
//        this.arguments = ca;
//        finished = false;
//        if(ca.cache == null){
//            throw new RuntimeException("Cannot restart as no cache location specified.");
//        }
//        cachePath = Paths.get(ca.cache);
//        if(ca.restart){
//            restart(Paths.get(ca.cache));
//        }
//    }
//
//    /**
//     * Set the path to the cached seeds and job ids
//     * @param cachePath
//     */
//    public void setCachePath(String cachePath){
//        setCachePath(Paths.get(cachePath));
//    }
//
//    public void setCachePath(Path cachePath){
//        this.cachePath = cachePath;
//    }
//
//    /**
//     * Get the completed or running jobs in the queue/history. Currently unused.
//     */
//    public List<Integer> getFailed(boolean completed) {
//        List<Integer> failed = new ArrayList<>();
//        for(Integer i : idSeeds.values()) {
//            List<String> messages = JqmClientFactory.getClient().getJobMessages(i);
//            for(String message : messages) {
//                if(message.equals(FAILED)){
//                    failed.add(i);
//                }
//            }
//        }
//        return failed;
//    }
//
//    public boolean hasFailed(int id) {
//        boolean failed = false;
//        List<String> messages = JqmClientFactory.getClient().getJobMessages(id);
//        for(String message : messages) {
//            if(message.equals(FAILED)){
//                failed = true;
//            }
//        }
//        return failed;
//    }
//
//    public boolean hasFailed(String seed) {
//        return hasFailed(idSeeds.get(seed));
//    }
//
//    /**
//     * Post a new crawler request to the server.
//     * @param seed
//     * @return
//     */
//    public int postJobRequest(String seed) {
//        JobRequest jr = JobRequest.create("Crawler","jp242");
//        jr.setKeyword1(seed);
//        jr.addParameters(buildParameters(seed));
//        return JqmClientFactory.getClient().enqueue(jr);
//    }
//
//    /**
//     * Check if the current id is currently running and if not submit the seed for crawling
//     * @param i
//     * @param seed
//     * @return
//     */
//    public int postJobRequest(int i, String seed) {
//        List<JobInstance> jobs = JqmClientFactory.getClient().getActiveJobs();
//        for(JobInstance job : jobs) {
//            if(job.getId().intValue() == i || job.getKeyword1().equals(seed)){
//                return -1;
//            }
//        }
//        return postJobRequest(seed);
//    }
//
//    /**
//     * Add a crawler for each seed to the queue and make a record of the currently
//     */
//    public void start() throws IOException {
//
//        for(String seed : arguments.seeds) {
//            int id = postJobRequest(seed);
//            idSeeds.put(seed,id);
//            saveJobQueue(cachePath);
//        }
//
//    }
//
//    public void scheduleNextPoll(long waitTime) {
//        scheduler.scheduleAtFixedRate(new JobPolling(),0,POLLWAIT, TimeUnit.SECONDS);
//    }
//
//    /**
//     * Restart the manager from the cached job ids.
//     * @param jobIdPath
//     */
//    public void restart(Path jobIdPath) {
//        try {
//            idSeeds = loadJobQueue(jobIdPath);
//        } catch (IOException e) {
//            throw new RuntimeException("Error when parsing the seed/jobid map.");
//        } catch (ParseException e) {
//            throw new RuntimeException("Error when parsing the seed/jobid map.");
//        }
//    }
//
//    public void saveJobQueue(Path jobIdPath) throws IOException {
//
//        JSONObject output = new JSONObject();
//        for(Map.Entry<String,Integer> entry : idSeeds.entrySet()) {
//            output.put(String.valueOf(entry.getKey()),entry.getValue());
//        }
//        try(BufferedWriter bw = new BufferedWriter(new FileWriter(jobIdPath.toFile()))){
//            bw.write(output.toString());
//        }
//
//    }
//
//    public HashMap<String,Integer> loadJobQueue(Path jobIdPath) throws IOException, ParseException {
//
//        try(BufferedReader br = new BufferedReader(new FileReader(jobIdPath.toFile()))) {
//            JSONParser jp = new JSONParser();
//            JSONObject json =  (JSONObject) jp.parse(br);
//            HashMap<String,Integer> idSeeds = new HashMap<>();
//            for(String key : json.keySet()) {
//                idSeeds.put(json.getString(key),Integer.valueOf(key));
//            }
//            return idSeeds;
//        }
//
//    }
//
//    /**
//     * Allows a new seed to be added to a potentially already runnning instance.
//     * @param seed
//     */
//    public void addNewSeed(String seed) {
//        for(String currSeed : idSeeds.keySet()) {
//            if(currSeed.equals(seed)) {
//                logger.warn("WARN: Seed already cached for crawling. No action taken");
//                return;
//            }
//        }
//        int id = postJobRequest(seed);
//        idSeeds.put(seed,id);
//        try {
//            saveJobQueue(cachePath);
//        } catch (IOException e) {
//            throw new RuntimeException("Could not cache job queue");
//        }
////        restartCrawl(id,1);
//    }
//
//    /**
//     * Simply stops re-running crawlers for the given seeds.
//     * Waits for all crawlers to finish.
//     */
//    public void stop() {
//        finished = true;
////        jobManager.yield();
//    }
//
//    /**
//     * Prevents new crawls being instantiated and kills all running crawls
//     */
//    public void stopNow() {
//        finished = true;
//        for(Integer id : idSeeds.values()) {
//            JqmClientFactory.getClient().killJob(id);
//            JqmClientFactory.getClient().cancelJob(id);
//        }
////        jobManager.yield();
//    }
//
////    public void restartCrawl(int id, long pollTime) {
////
////        // If finished - then simply don't poll anymore job requests.
////        if(finished) {
////            return;
////        }
////        if(jobManager.hasFailed(id)){
////            // log the failure, but do not restart. Might be a problem that needs fixing.
////        }
////        if(jobManager.hasSucceeded(id) || jobManager.hasEnded(id)){
////            enqueue(idSeeds.get(id));
////        }
////
////    }
//
//    public class JobPolling implements Runnable {
//
//        @Override
//        public void run() {
//
//            Set<String> potentialSeeds = new HashSet<>();
//            for(JobInstance job : JqmClientFactory.getClient().getActiveJobs()){
//                int id = job.getId();
//                String seed = job.getKeyword1();
//                if(!idSeeds.containsKey(seed) && !idSeeds.containsValue(id)) {
//                    potentialSeeds.add(seed);
//                }
//            }
//
//            for(String seed : potentialSeeds){
//                if(!hasFailed(idSeeds.get(seed)) && !hasFailed(seed)) {
//                    idSeeds.put(seed,postJobRequest(seed));
//                    try {
//                        saveJobQueue(cachePath);
//                    } catch (IOException e) {
//                        throw new RuntimeException("Could not write job cache");
//                    }
//                }
//            }
//        }
//
//    }
//
//    public Map<String,String> buildParameters(String seed) {
//        Map<String,String> enqueueParams = new HashMap<String,String>();
//        enqueueParams.put(CrawlerArguments.AGENT,arguments.userAgent);
//        enqueueParams.put(CrawlerArguments.DB,arguments.crawldb);
//        enqueueParams.put(CrawlerArguments.DEPTH,String.valueOf(arguments.depth));
////        for(String filter : arguments.urlFilters){
////            enqueueParams.put(CrawlerArguments.FILTER,filter);
////        }
//        enqueueParams.put(CrawlerArguments.POLITE,String.valueOf(arguments.polite));
//        enqueueParams.put(CrawlerArguments.SITEMAP, (arguments.ignoreSitemap) ? String.valueOf(1) : String.valueOf(0));
//        enqueueParams.put(CrawlerArguments.ROBOTS,(arguments.ignoreRobots) ? String.valueOf(1) : String.valueOf(0));
//        enqueueParams.put(CrawlerArguments.ID, arguments.id);
//        enqueueParams.put(CrawlerArguments.THREADS,String.valueOf(arguments.threadsPerSeed));
//        enqueueParams.put(CrawlerArguments.SEED,seed);
//        return enqueueParams;
//    }
//
//
//
//    public int enqueue(String seed){
//        return jobManager.enqueue("Crawler",// application name
//                arguments.userAgent,    // user
//                null,               // mail
//                null,               // session Id
//                "Crawler",          // application
//                "NorconexCrawler",  // module
//                null,               // keyword 1
//                null,               // keyword 2
//                null,               // keyword 3
//                buildParameters(seed)); // program params
//    }
//
//    //    public static void main(String[] args) {
////        CrawlerArguments ca = new CrawlerArguments();
////        new JCommander().newBuilder()
////                .addObject(ca)
////                .build()
////                .parse(args);
//////        enqueueCrawlerManager(ca.userAgent, ca.crawldb, ca.id, ca.depth, new ArrayList<>(), ca.threadsPerSeed, ca.ignoreRobots, ca.ignoreSitemap, ca.polite, ca.seeds.get(0));
////        JQManagement jq = new JQManagement(ca);
////        jq.start();
////    }
//
//    public static void main(String[] args) {
//        String user = "test52";
//        String crawldb = "/Users/jp242/Documents/Projects/Crawler-Upgrade/testdb";
//        String id = "Crawler";
//        int depth = 1;
//        List<String> filters = new ArrayList<>();
//        int threads = 5;
//        boolean ignoreRobots = false;
//        boolean ignoreSitemap = false;
//        long polite= 300;
//        String seed = "http://www.taglaboratory.org/";
//        CrawlerArguments cargs = new CrawlerArguments();
//        cargs.seeds = Arrays.asList(seed);
//        cargs.userAgent = user;
//        cargs.crawldb = crawldb;
//        cargs.id = id;
//        cargs.depth = depth;
//        cargs.urlFilters = filters;
//        cargs.threadsPerSeed = threads;
//        cargs.ignoreRobots = ignoreRobots;
//        cargs.ignoreSitemap = ignoreSitemap;
//        cargs.polite = polite;
//        cargs.cache = "";
//        JQManagement jqman = new JQManagement(cargs,"http://localhost:56379");
//        try {
//            jqman.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //    public static int enqueueCrawlerManager(String userAgent,
////                                            String crawldb,
////                                            String id,
////                                            int depth,
////                                            List<String> filters,
////                                            int threadsPerSeed,
////                                            boolean ignoreRobots,
////                                            boolean ignoreSiteMap,
////                                            long politenessDelay,
////                                            String seeds) {
////        JobRequest jr = JobRequest.create("CrawlerManager","CASM");
////        Map<String,String> parameters = new HashMap<>();
////        parameters.put(CrawlerArguments.THREADS,String.valueOf(threadsPerSeed));
////        parameters.put(CrawlerArguments.ID,id);
////        parameters.put(CrawlerArguments.ROBOTS,(ignoreRobots) ? String.valueOf(1) : String.valueOf(0));
////        parameters.put(CrawlerArguments.SITEMAP,(ignoreSiteMap) ? String.valueOf(1) : String.valueOf(0));
////        parameters.put(CrawlerArguments.POLITE,String.valueOf(politenessDelay));
//////        parameters.put(CrawlerArguments.FILTER,null);
////        parameters.put(CrawlerArguments.DEPTH,String.valueOf(depth));
////        parameters.put(CrawlerArguments.AGENT,userAgent);
////        parameters.put(CrawlerArguments.DB,crawldb);
////        parameters.put(CrawlerArguments.SEED,seeds);
////        jr.addParameters(parameters);
////        return jr.submit();
////
////    }
//
//}
