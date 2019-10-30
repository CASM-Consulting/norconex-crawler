package uk.ac.susx.tag.norconex.jobqueuemanager;

import com.beust.jcommander.Parameter;

import java.util.List;

public class CrawlerArguments {

    @Parameter(names = {SingleSeedCollector.USERAGENT}, description = "The user agent provided to sites.",
            required = true)
    public String userAgent;

    @Parameter(names = {SingleSeedCollector.CRAWLB, "--crawl-db"}, description = "The directory to store the crawler output.",
            required = true)
    public String crawldb;


    @Parameter(names = {SingleSeedCollector.ID,"--collectorId"}, description = "The id of the collector instance.",
            required = true)
    public String id;

    @Parameter(names = {SingleSeedCollector.DEPTH,"--depth"}, description = "The depth of the crawl.",
            required = true)
    public int depth;

    @Parameter(names = {SingleSeedCollector.FILTER,"--url-filter"}, description = "Regex patterns to match and filter urls/domains.")
    public String urlFilter;

    @Parameter(names = {SingleSeedCollector.THREADS,"--threads-per-seed"}, description = "The number of threads to provide for each seed.")
    public int threadsPerSeed;

    @Parameter(names = {SingleSeedCollector.ROBOTS,"--ignore-robots"}, description = "Whether or not to ignore robots instructions.",
            arity = 1)
    public boolean ignoreRobots;

    @Parameter(names = {SingleSeedCollector.SITEMAP,"--ignore-sitemap"}, description = "Whether or not to ignore sitemap instructions.",
            arity = 1)
    public boolean ignoreSitemap;

    @Parameter(names = {SingleSeedCollector.POLITENESS,"--politeness-delay"}, description = "The politness delay between calls to a site",
            required = true)
    public long polite;

    @Parameter(names = {SingleSeedCollector.SEED}, description = "The seed(s) url to begin crawling from.",
            required = true)
    public List<String> seeds;

}