package uk.ac.susx.tag.norconex;

import org.junit.Test;
import uk.ac.susx.tag.norconex.crawlpolling.SubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerArguments;
import uk.ac.susx.tag.norconex.jobqueuemanager.DBTableSubmissionService;
import uk.ac.susx.tag.norconex.jobqueuemanager.SingleSeedCollector;
import uk.ac.susx.tag.norconex.jobqueuemanager.Straight2DBCollector;
import uk.ac.susx.tag.norconex.utils.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;


public class Straight2DBCollectorSubmissionTest {


    @Test
    public void TestStraoghtToDBSubmitter() throws IOException {

        Straight2DBCollector collector = new Straight2DBCollector();
        String seed = "https://www.bbc.co.uk/news";
        CrawlerArguments ca = new CrawlerArguments();
        ca.seeds = Arrays.asList(seed);
        ca.crawldb= "/Users/jp242/Desktop";
        ca.depth=3;
        ca.ignoreRobots = false;
        ca.ignoreSitemap = false;
        ca.polite = 400;
//        ca.scrapers = "/Users/jp242/Documents/Projects/ACLED/ManualScrapers/demoscrapers";
//        ca.scraper = "newagebdnet";
        ca.threadsPerSeed = 2;
//        ca.urlFilter = ".*";
        ca.userAgent = "taglab";
        ca.id = "testspring";
        ca.index = true;
        ca.crawldbProps = "/Users/jp242/Documents/Projects/JQM-Crawling/jqm_root/conf/basic.properties";

        try {
            collector.runCollector(ca);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}
