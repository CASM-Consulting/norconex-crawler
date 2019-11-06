package uk.ac.susx.tag.norconex;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerArguments;
import uk.ac.susx.tag.norconex.jobqueuemanager.SingleSeedCollector;

import java.io.File;
import java.net.URISyntaxException;

public class TestCLRunning {

    @Test
    public void testCLRun() {

        CrawlerArguments ca = new CrawlerArguments();
        String seed = "http://www.taglaboratory.org/";
        String[] args = new String[]{SingleSeedCollector.USERAGENT,"m52",SingleSeedCollector.CRAWLB,"tests/crawldb",
        SingleSeedCollector.DEPTH,"0",SingleSeedCollector.POLITENESS,"300",
        SingleSeedCollector.THREADS,"2",SingleSeedCollector.ID, "singlseedcl", SingleSeedCollector.SEED, seed};
        new JCommander().newBuilder()
                .addObject(ca)
                .build()
                .parse(args);

        SingleSeedCollector ssc = new SingleSeedCollector(ca.userAgent,new File(ca.crawldb),ca.id,
                ca.depth,ca.urlFilter,ca.threadsPerSeed,
                ca.ignoreRobots,ca.ignoreSitemap,ca.polite,ca.seeds.get(0));

        try {
            ssc.start();
        } catch (URISyntaxException e) {
            Assert.fail();
        }
    }
}
