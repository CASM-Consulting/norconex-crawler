package uk.ac.susx.tag.norconex.scraper;

import com.beust.jcommander.JCommander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.susx.tag.norconex.scraping.GeneralSplitterFactory;
import uk.ac.susx.tag.norconex.scraping.POJOHTMLMatcherDefinition;
import uk.ac.susx.tag.norconex.utils.IncorrectScraperJSONException;
import uk.ac.susx.tag.norconex.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Class designed to sit on a JQM queue and scrape web pages stored in the database
 */
public class ScraperJob {

    protected static final Logger logger = LoggerFactory.getLogger(ScraperJob.class);

    private ExecutorService service;
    private static final int THREADS = 10;
    private boolean complete = false;

    private int ID;

    // global scraper data
    private Path scraperDirectory;

    // Used if you wish the pre-processor to contain all scrapers.
    public Map<String, GeneralSplitterFactory> scrapersJson;

    private int jobId;

    public ScraperJob(Path scraperDirectory) {
        try {
            initScrapers(scraperDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IncorrectScraperJSONException e) {
            e.printStackTrace();
        }
        service = Executors.newFixedThreadPool(THREADS);
    }

    /**
     * Shutdown the scraping service.
     * @throws InterruptedException
     */
    public void shutDown() throws InterruptedException {
        complete = true;
        service.shutdown();
        service.awaitTermination(10, TimeUnit.MINUTES);
    }

    /**
     * Base factory class for building scraper instances.
     * @param <A>
     */
    public abstract class ScraperFactory<A extends BaseScraper>{

        public abstract A create();

    }

    /**
     * Initialise and build all scrapers in a single directory.
     * @param scrapersLocation
     */
    private void initScrapers(Path scrapersLocation) throws IOException, IncorrectScraperJSONException {

        List<Path> scrapers = Files.walk(scrapersLocation)
                .filter(file -> file.getFileName().toString().equals("job.json"))
                .collect(Collectors.toList());

        for (Path path : scrapers) {

                File file = path.toFile();
                String processed = Utils.processJSON(file);
                Map<String, List<Map<String, String>>> scraperDefs = buildScraperDefinition(GeneralSplitterFactory.parseJsonTagSet(processed));
                logger.info("Adding scraper: " + file.getParentFile().getName());
                scrapersJson.put(file.getParentFile().getName(), new GeneralSplitterFactory(scraperDefs));
                logger.info("Added scraper for: " + file.getParentFile().getName() + " " + scrapersJson.get(file.getParentFile().getName().replace(".json", "")));

        }
    }

    /**
     * Transform json pojo object to splitter structure
     * @param matcherList
     * @return
     */
    public static Map<String, List<Map<String, String>>> buildScraperDefinition(List<POJOHTMLMatcherDefinition> matcherList) {

        Map<String, List<Map<String, String>>> fields = new HashMap<>();
        for(POJOHTMLMatcherDefinition matcher : matcherList) {
            List<Map<String, String>> tags = matcher.getTagDefinitions();
            fields.put(matcher.field,tags);
        }
        return fields;

    }

    public static void main(String[] args) {

        String[] corrArgs = Utils.buildArguments(args);
        ScraperArguments scraperArguments = new ScraperArguments();
        new JCommander().newBuilder()
                .addObject(scraperArguments)
                .build()
                .parse(corrArgs);

        ScraperJob scraperJob = new ScraperJob(Paths.get(scraperArguments.scraperDir));

    }


}
