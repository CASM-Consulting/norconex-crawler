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
        CrawlerArguments ca = new CrawlerArguments();
        String seed = "http://www.taglaboratory.org/";
        SingleSeedCollector ssc = new SingleSeedCollector("m5", new File("tests/crawldb"),"singleseed",0,null,2,false,false,300,seed);
        try {
            ssc.start();
        } catch (URISyntaxException e) {
            Assert.fail();
        }
    }

}
