package uk.ac.susx.tag.norconex.jobqueuemanager;

// google imports
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

// json imports
import org.json.JSONArray;
import org.json.JSONObject;

// java imports
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import uk.ac.casm.jqm.manager.IndependentPollingManager;
import uk.ac.casm.jqm.manager.SubmissionService;

public class CrawlerSubmissionService extends SubmissionService {

    public static final String SEED  = "seed";
    public static final String SEEDS = "seeds";

    public static final String LINK       = "Link";
    public static final String SOURCE     = "Source Name";
    public static final String COUNTRIES  = "Countries";

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
    public void submitSeeds(JSONArray seeds) throws IOException {
        for(int i = 0; i < seeds.length(); i++) {
            submitSeed((HashMap<String,String>) new ObjectMapper().readValue(seeds.getJSONObject(i).getJSONObject(SEED).toString(),HashMap.class));
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
        jr.setKeyword1(seed.get(LINK));
        this.submitJobRequest(jr);

    }

    public static void main(String[] args) {
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
