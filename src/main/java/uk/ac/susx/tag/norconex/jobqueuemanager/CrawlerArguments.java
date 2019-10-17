package uk.ac.susx.tag.norconex.jobqueuemanager;

import com.beust.jcommander.Parameter;

import java.util.List;

public class CrawlerArguments {

    public static final String CACHE = "-cache";
    @Parameter(names = {CACHE}, description = "The location of a previous cache of jobs and seeds. (Required if restart is true)",
        required = true)
    public String cache;

    public static final String RESTART = "-restart";
    @Parameter(names = {RESTART}, description = "Whether or not to restart the monitoring service.")
    public boolean restart;

    public static final String AGENT = "-agent";
    @Parameter(names = {AGENT}, description = "The user agent provided to sites.",
            required = true)
    public String userAgent;

    public static final String DB = "-db";
    @Parameter(names = {DB, "--crawl-db"}, description = "The directory to store the crawler output.",
            required = true)
    public String crawldb;


    public static final String ID = "-id";
    @Parameter(names = {ID,"--collectorId"}, description = "The id of the collector instance.",
            required = true)
    public String id;

    public static final String DEPTH = "-d";
    @Parameter(names = {DEPTH,"--depth"}, description = "The depth of the crawl.",
            required = true)
    public int depth;

    public static final String FILTER = "-filter";
    @Parameter(names = {FILTER,"--url-filter"}, description = "Regex patterns to match and filter urls/domains.")
    public List<String> urlFilters;

    public static final String THREADS = "-threads";
    @Parameter(names = {THREADS,"--threads-per-seed"}, description = "The number of threads to provide for each seed.")
    public int threadsPerSeed;

    public static final String ROBOTS = "-robots";
    @Parameter(names = {ROBOTS,"--ignore-robots"}, description = "Whether or not to ignore robots instructions.",
            required = true)
    public boolean ignoreRobots;

    public static final String SITEMAP = "-sitemap";
    @Parameter(names = {SITEMAP,"--ignore-sitemap"}, description = "Whether or not to ignore sitemap instructions.",
            required = true)
    public boolean ignoreSitemap;

    public static final String POLITE = "-polite";
    @Parameter(names = {POLITE,"--politeness-delay"}, description = "The politness delay between calls to a site",
            required = true)
    public long polite;

    public static final String SEED = "-seed";
    @Parameter(names = {SEED}, description = "The seed(s) url to begin crawling from.",
            required = true)
    public List<String> seeds;

}