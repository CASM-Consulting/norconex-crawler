package uk.ac.susx.tag.norconex;

import org.junit.Test;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerSubmissionService;
import uk.ac.susx.tag.norconex.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class TestSubmissionService {

    @Test
    public void testSubmissionService(){
        String properties = "/Users/jp242/Documents/Projects/JQM-Crawling/jqm_root/conf/acled-crawlmanager.properties";
        String links = "/Users/jp242/Documents/Projects/ACLED/ManualScrapers/demo-seed-list.json";

        Path linkPath = Paths.get(links);
        Properties props = Utils.getProperties(properties);

        CrawlerSubmissionService css = new CrawlerSubmissionService(props);

        try {
            css.submitSeeds(CrawlerSubmissionService.loadSeeds(linkPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
