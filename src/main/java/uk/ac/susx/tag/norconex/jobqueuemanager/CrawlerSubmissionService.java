package uk.ac.susx.tag.norconex.jobqueuemanager;

// google imports
import com.enioka.jqm.api.JobRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

// json imports
import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

// java imports
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.crawlpolling.SubmissionService;

public class CrawlerSubmissionService extends SubmissionService {

    protected static final Logger logger = LoggerFactory.getLogger(CrawlerSubmissionService.class);

    public static final String SEED  = "seed";
    public static final String SEEDS = "seeds";

    public static final String LINK       = "LINK";
    public static final String NAME       = "NAME";
    public static final String COUNTRIES  = "COUNTRIES";
    public static final String SCRAPER    = "SCRAPER";

        public static final String CRAWLERJOB = "SpringCollector";
    public static final String USER       = "crawler-submission-service";

    public CrawlerSubmissionService(Properties props) {
        super(props);
    }

    public static JSONArray loadSeeds(Path location) throws IOException {
        String json = Files.toString(location.toFile(), Charset.defaultCharset());
        return new JSONObject(json).getJSONArray(SEEDS);
    }

    /**
     * @param seeds seeds as JSONArray of seeds, which de-serliase into a map
     *              with seed and metadata. See @submitSeed
     * @throws IOException
     */
    public void submitSeeds(JSONArray seeds) {
        System.out.println(seeds.length());
        for(int i = 0; i < seeds.length(); i++) {
            try {
                submitSeed((HashMap<String,String>) new ObjectMapper().readValue(seeds.getJSONObject(i).getJSONObject(SEED).toString(),HashMap.class));
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (IOException e) {
                logger.error("ERROR: failed to submit seed: " + seeds.getJSONObject(i).getJSONObject(SEED).toString());
                System.out.println(seeds.getJSONObject(i).getJSONObject(SEED).toString());
                continue;
            } catch (Exception e) {
                logger.error("ERROR: failed to submit seed: " + seeds.getJSONObject(i).getJSONObject(SEED).toString());
                e.printStackTrace();
                System.out.println(seeds.getJSONObject(i).getJSONObject(SEED).toString());
                continue;
            }
            System.out.println(i);
        }
    }

    /**
     * @param seed Map containing the seed url to crawl,
     *            readable source name and countries the source covers
     */
    public void submitSeed(Map<String,String> seed) {

        JobRequest jr = JobRequest.create(CRAWLERJOB, USER);
        jr.addParameter(SingleSeedCollector.SEED, SingleSeedCollector.SEED + " " + seed.get(LINK));
        jr.addParameter(SingleSeedCollector.ID, SingleSeedCollector.ID + " " + seed.get(LINK));
        jr.addParameter(CrawlerArguments.SCRAPER, CrawlerArguments.SCRAPER + " " + resolveScraperName(seed.get(SCRAPER)));
        jr.addParameter(CrawlerArguments.SOURCEDOMAIN, CrawlerArguments.SOURCEDOMAIN + " " + seed.get(CrawlerArguments.SOURCEDOMAIN));
        jr.setKeyword1(seed.get(LINK));
        this.submitJobRequest(jr);

    }

    /**
     * Makes sure that json suffix is applied to the scraper name.
     * @param scraper
     * @return
     */
    private String resolveScraperName(String scraper) {
        if(!scraper.endsWith(".json")) {
            return scraper + ".json";
        }
        return scraper;
    }

//    public

    private HashMap<String, SimpleResponse> validateSeeds(JSONArray seeds) throws IOException {

        HashMap<String,SimpleResponse> responses = new HashMap<>();
        for(int i = 0; i < seeds.length();  i++) {
            HashMap<String, String> seedMap = (HashMap<String, String>) new ObjectMapper().readValue(seeds.getJSONObject(i).getJSONObject(SEED).toString(), HashMap.class);
            final String seed = seedMap.get(SingleSeedCollector.SEED);
//            responses.put(seed,validateSeed(seed,validateSeed(seed));
        }
        return responses;
    }

    private SimpleResponse validateSeed(String seed) {

        final SimpleResponse response = new SimpleResponse();

        URL url;
        try {
            url = new URL(seed);
        } catch (MalformedURLException e) {
            response.valid = false;
            return response;
        }

        try {
            URLConnection connection = url.openConnection();
            connection.connect();
//            connection.getRe
        } catch (IOException e) {
            e.printStackTrace();
        }


        return response;
    }

    public class SimpleResponse {

        boolean valid;
        boolean timeout;
        boolean failed;
        String responseCode;

    }


    public static Options getCLIOptions() {
        final Options options = new Options();
        options.addOption("-v", false, "Specify if you wish to simply validate your seed list.");
        options.addOption("-vf", false, "Will write out to a csv detailing all urls failing validation and why.");
        return options;
    }

    /**
     * Simple convenience method to normalise json keys and increase robustness.
     * @return
     */
    private String normaliseParam(String key){
        return key.toLowerCase().trim().replaceAll("\\s+","-");
    }

    public static void main(String[] args) {
//        CommandLineParser clp = new DefaultParser();
//
//        try {
//            CommandLine cli = clp.parse(getCLIOptions(),args);
//        } catch (ParseException e) {
//            new RuntimeException("Failed tp parse command line parameters: " + e.getLocalizedMessage());
//        }

        Path links = Paths.get(args[0]);
        Properties props = CrawlerSubmissionService.getProperties(args[1]);
        CrawlerSubmissionService css = new CrawlerSubmissionService(props);
        try {
            css.submitSeeds(CrawlerSubmissionService.loadSeeds(links));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
