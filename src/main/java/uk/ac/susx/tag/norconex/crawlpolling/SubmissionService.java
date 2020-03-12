package uk.ac.susx.tag.norconex.crawlpolling;

// jqm imports

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.utils.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

// java imports

/**
 * Intended to be used as a service for configuring and submitting jobs enmasse
 */
public abstract class SubmissionService {

    protected static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    public static final String SEED  = "seed";
    public static final String SEEDS = "seeds";

    public static final String SCRAPER    = "SCRAPER";
    public static final String SOURCE     = "SOURCE";
    public static final String LINK       = "LINK";


    protected final Properties properties;
    private JobInstance manager;

    public SubmissionService() {
        this(new Properties());
    }

    public SubmissionService(Properties properties) {
        this.properties = properties;
    }

    public SubmissionService(String propertiesLocation) {
        this(Utils.getProperties(propertiesLocation));
    }

    public SubmissionService(Path propertiesLocation) {
        properties = Utils.getProperties(propertiesLocation);
    }

    public void setManager(int jobId) {
        manager = JqmClientFactory.getClient().getJob(jobId);
    }

    public int submitJobRequest(JobRequest request) {

        JqmClientFactory.setProperties(properties);         // reiterated each time to ensure it remains up-to-date

        return JqmClientFactory.getClient().enqueue(request);

    }

    public static JSONArray loadSeeds(Path location) throws IOException {
        String json = Files.toString(location.toFile(), Charset.defaultCharset());
        return new JSONObject(json).getJSONArray(SEEDS);
    }

    public static int submitJobRequest(JobRequest request, Properties properties) {

        JqmClientFactory.setProperties(properties);
        return JqmClientFactory.getClient().enqueue(request);

    }

    /**
     * @param seeds seeds as JSONArray of seeds, which de-serliase into a map
     *              with seed and metadata. See @submitSeed
     * @throws IOException
     */
    public void submitSeeds(JSONArray seeds, String jobDef) {
        System.out.println(seeds.length());
        for(int i = 0; i < seeds.length(); i++) {
            try {
                Map<String,String> seed = (HashMap<String,String>) new ObjectMapper().readValue(seeds.getJSONObject(i).getJSONObject(SEED).toString(),HashMap.class);
                System.out.println(seed);
                submitSeed(seed, jobDef);
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

    public abstract void submitSeed(Map<String,String> params, String jobDef);

}
