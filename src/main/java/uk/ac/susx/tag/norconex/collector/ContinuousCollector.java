package uk.ac.susx.tag.norconex.collector;

import com.norconex.collector.http.HttpCollector;
import com.norconex.collector.http.HttpCollectorConfig;

import uk.ac.susx.tag.norconex.controller.ContinuousController.ContinuousCollectorListener;

public class ContinuousCollector extends HttpCollector {
	

	public ContinuousCollector(HttpCollectorConfig collectorConfig) {
		super(collectorConfig);
	}
		
	/**
	 * Set the collector to finished to prevent restart and stop the crawler.
	 */
	public void shutdown() {
		this.stop();
	}
	
	public static HttpCollectorConfig createCollectorConfig(String id, ContinuousCollectorListener listener) {
		HttpCollectorConfig config = new HttpCollectorConfig();
		config.setCollectorListeners(listener);
		config.setId(id);
		return config;
	}

}
