package uk.ac.susx.tag.norconex;

import com.norconex.collector.http.HttpCollectorConfig;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import org.junit.Test;
import uk.ac.susx.tag.norconex.collector.BasicCollector;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class TestBasicCollector {

    @Test
    public void testBasicCollector() {
        HttpCrawlerConfig config = BasicCollector.crawlerConfig("support@casmconsulting.co.uk",
                1,2,new File("/Users/jp242/Desktop/test-crawl"),
                false,false,"test-crawler",
                new ArrayList<>(),new ArrayBlockingQueue<>(1000),true, 300);

        config.setStartURLs("http://shekulli.com.al");

        HttpCollectorConfig collectorconfig = new HttpCollectorConfig();
        collectorconfig.setId("test-collector");
        collectorconfig.setCrawlerConfigs(config);

        BasicCollector collector = new BasicCollector(collectorconfig);

        collector.start(false);

    }


}
