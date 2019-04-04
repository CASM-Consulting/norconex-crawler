package uk.ac.susx.tag.norconex.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.ICollectorConfig;
import com.norconex.collector.http.HttpCollector;
import com.norconex.collector.http.HttpCollectorConfig;

import uk.ac.susx.tag.norconex.controller.ContinuousController.ContinuousCollectorListener;

public class ContinuousCollector extends HttpCollector {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousCollector.class);
	
	private ContinuousCollectorListener listener;
	private boolean finish;

	public ContinuousCollector(HttpCollectorConfig collectorConfig, ContinuousCollectorListener listener) {
		super(collectorConfig);
		this.finish = false;
		this.listener = listener;
	}
		
	/**
	 * Set the collector to finished to prevent restart and stop the crawler.
	 */
	public void shutdown() {
		logger.info("Shutting down continuous crawl");
		this.finish = true;
		this.stop();
	}
	
	
	
	public ICollectorConfig createCollectorConfig() {
		HttpCollectorConfig config = new HttpCollectorConfig();
		config.setCollectorListeners(listener);
		config.setId("0");
		return config;
	}
	
//	/**
//	 * Creates an infinite loop causing the crawler to continually start crawling again when finished.
//	 * This can only be interrupted by manually requesting the crawler to stop permanently.
//	 * @author jp242
//	 *
//	 */
//	public class ContinuousCollectorListener implements ICollectorLifeCycleListener {
//
//		@Override
//		public void onCollectorStart(ICollector collector) {}
//
//		@Override
//		public void onCollectorFinish(ICollector collector) {
//			if(!finish) {
//				collector.start(false);
//			}			
//		}
//		
//	}

}
