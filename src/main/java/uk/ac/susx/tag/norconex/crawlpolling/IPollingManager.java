package uk.ac.susx.tag.norconex.crawlpolling;

import java.util.Map;

public interface IPollingManager {

    public boolean isFinished();

    public void start();

    public void start(long pollInterval);

    public void shutdown();

    public void shutdownNow();

    public void restart();

    public int postJobRequest();

    public Map<String,Integer> buildParameters();

    public String getAppName();

    public String getUsername();


}
