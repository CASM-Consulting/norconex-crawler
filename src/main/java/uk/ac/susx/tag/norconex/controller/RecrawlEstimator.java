package uk.ac.susx.tag.norconex.controller;

/**
 * Used to collect runtime information about the crawl change frequencies.
 * Used to calculate re-crawl frequency
 * 
 * Based on: Keeping a Search Engine Index Fresh: Risk and optimality in estimating refresh rates for web pages
 * D. Ford, C. Grimes, E. Tassone
 * 
 * @author jp242
 *
 */
public class RecrawlEstimator {
	
	// rate of change - parameter to control crawler component
	private double rate = 1.0;							
	
	// Presets based on estimated likelihood of change to initialise and scale system fit for purpose (i.e. low to medium scale crawling)
	public static final double r 		= 0.33;			// simulated delta interval ratio
	public static final double priorOne = 1; 			// simulated prior of frequently changing page
	public static final double priorTwo = 60; 			// simulated prior of slower changing page (2.5 days)
	public static final double defaultInterval = 48; 	// default minimum time period (48 hours)
	public static final double minC1	 = 12;			// Min cost for calculating interval (12 hours)
	public static final double maxC2     = 336;			// Max cost for calculating interval (2 weeks)
	
	private double c1 = minC1;			// Lower bound cost of page refresh frequency
	private double c2 = maxC2;			// Upper bound cost of page refresh frequency
	
	public RecrawlEstimator(double rate) {
		this.rate = rate;
	}
	
	public void setC1(double C1) {
		this.c1 = (validC(C1)) ? C1 : minC1;
	}
	
	public void setC2(double C2) {
		this.c2 = (validC(C2)) ? C2 : maxC2;
	}
	
	/**
	 * Check new c value is within fixed bounds to prevent over-staleness or over-frequent crawling
	 * @param c
	 * @return
	 */
	private boolean validC(double c) {
		return (c >= minC1 && c <= maxC2) ? true : false;
	}
	
	/**
	 * Calculates the estimated interval for a given web page
	 * @param delta page specific delta based on estimated likelihood of change
	 * @return
	 */
	public double calculateInterval(double delta) {
		return (c1/delta) * rate;
	}
	
	/**
	 * @return The estimated delta (re-crawl interval) based on previous crawl statistics or defaults if none available
	 */
	public double estimateDelta() {
		return 0;
	}
	
	
	
	
	
}
