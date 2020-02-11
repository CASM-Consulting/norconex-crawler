package uk.ac.susx.tag.norconex.jobqueuemanager;

import com.beust.jcommander.Parameter;

import java.util.List;

public class CrawlerArguments {

    public static final String SCRAPEDARTICLE = "scraped.article";
    public static final String SCRAPEDATE = "scraped.date";
    public static final String SCRAPEDTITLE = "scraped.title";

    @Parameter(names = {SingleSeedCollector.USERAGENT}, description = "The user agent provided to sites.",
            required = true)
    public String userAgent;

    @Parameter(names = {SingleSeedCollector.CRAWLB, "--crawl-db"}, description = "The directory to store the crawler output.",
            required = true)
    public String crawldb;

    public static final String SCRAPER = "casm.jqm.crawling.scraper";
    @Parameter(names = {SCRAPER, "--scraper"}, description = "The location of a single scraper ruleset to be used by a crawler.")
    public String scraper;

    public static final String SCRAPERS = "casm.jqm.scraping.scrapers.dir";
    @Parameter(names = {SCRAPERS, "--scrapers-dir"}, description = "The directory of the json scraper rulesets.")
    public String scrapers;


    public static final String SOURCENAME = "casm.jqm.source.name";
    @Parameter(names = {SOURCENAME, "--source-name"}, description = "The name of the source being crawled.")
    public String source;

    public static final String COUNTRIES = "casm.jqm.countries";
    @Parameter(names = {COUNTRIES, "--countries"}, description = "The countries the source covers.")
    public String countries;

    @Parameter(names = {SingleSeedCollector.ID,"--collectorId"}, description = "The id of the collector instance.",
            required = false)
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

    public static final String SOURCEDOMAIN = "casm.jqm.sourceDomain";
    @Parameter(names = {SOURCEDOMAIN}, description = "Allows one to specify a source or domain that is different from the starting seed.")
    public String sourcedomain;

    @Parameter(names = SingleSeedCollector.INDEXONLY, description = "Specifies whether to only index the site or process the send the output to disc.")
    public boolean index = false;

    public static final String LOCALSPRINGPROPS = "--spring.config.name=application-local.properties";
    @Parameter(names = {LOCALSPRINGPROPS}, description = "Specify whether to use the local spring boot properties or server side (i.e. pg-bouncer)")
    public boolean local = false;

    public static final String CRAWLDBPROPS = "casm.jqm.crawling.DBprops";
    @Parameter(names = {CRAWLDBPROPS}, description = "Specifies a properties file containing the crawler database commiter config.")
    public String crawldbProps;

    public static final String CONTENTHASH = "pageContentsHash";

}