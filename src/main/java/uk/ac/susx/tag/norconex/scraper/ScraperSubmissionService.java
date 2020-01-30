package uk.ac.susx.tag.norconex.scraper;

import com.beust.jcommander.JCommander;
import com.enioka.jqm.api.JobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.crawlpolling.SubmissionService;
import uk.ac.susx.tag.norconex.utils.Utils;

import java.util.Map;
import java.util.Properties;

public class ScraperSubmissionService extends SubmissionService {

    protected static final Logger logger = LoggerFactory.getLogger(ScraperSubmissionService.class);

    public static final String SCRAPERJOB = "WebScraper";
    public static final String USER       = "crawler-submission-service";

    private final int numScrapers;
    private final String scraperDir;
    private final Properties props;
    private final String queueName;


    public ScraperSubmissionService(int numScrapers, String scraperDir,
                                    Properties props, String queueName) {

        this.scraperDir  = scraperDir;
        this.numScrapers = numScrapers;
        this.props = props;
        this.queueName =  queueName;

    }

    public void submitScrapers() {
        System.out.println("Sumbitting " + numScrapers + " to queue - " + queueName);
        for(int i=0; i < numScrapers; i++) {
            System.out.println(i);
            logger.info("Submitting scraper " + i);
        }
    }


    /**
     * @param seed Map containing the seed url to crawl,
     *            readable source name and countries the source covers
     */
    public void submitScraper(Map<String,String> seed) {

        JobRequest jr = JobRequest.create(SCRAPERJOB, USER);
        jr.setQueueName(queueName);
        jr.addParameter(ScraperArguments.SCRAPERDIR, ScraperArguments.SCRAPERDIR + " " + scraperDir);
        jr.setKeyword1("ScrapingJob");
        this.submitJobRequest(jr);

    }


    public static void main(String[] args) {

        String[] corrArgs = Utils.buildArguments(args);
        ScraperArguments scraperArguments = new ScraperArguments();
        new JCommander().newBuilder()
                .addObject(scraperArguments)
                .build()
                .parse(corrArgs);

        Properties props = SubmissionService.getProperties(scraperArguments.propsPath);
        ScraperSubmissionService scraperSubmitter = new ScraperSubmissionService(scraperArguments.numScrapers,
                scraperArguments.scraperDir,props,scraperArguments.queueNAme);

        scraperSubmitter.submitScrapers();

    }
}
