package uk.ac.susx.tag.norconex;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerArguments;
import uk.ac.susx.tag.norconex.jobqueuemanager.SingleSeedCollector;

import java.io.File;
import java.net.URISyntaxException;

public class TestSingleSeedCrawler {

    @Test
    public void testSingleSeedCrawler() {
        String seed = "http://en.annahar.com/section/184-lebanon";
        SingleSeedCollector ssc = new SingleSeedCollector("casmconsulting.co.uk", new File("/home/sw206/git/springcrawler/testcrawldb"),"singleseed",5,null,2,false,false,300,seed);
        ssc.start();
    }

}
