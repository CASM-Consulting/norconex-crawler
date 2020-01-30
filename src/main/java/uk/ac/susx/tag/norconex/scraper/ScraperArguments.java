package uk.ac.susx.tag.norconex.scraper;

import com.beust.jcommander.Parameter;

public class ScraperArguments {


    private static final String NUMSCRAPERS = "casm.jqm.scraper.num";
    @Parameter(names = {NUMSCRAPERS}, description = "The number of scraper jobs to put on the queue.")
    public int numScrapers;


    public static final String SCRAPERDIR = "casm.jqm.scraper.directory";
    @Parameter(names = {SCRAPERDIR}, description = "The location of the scraper rulesets.")
    public String scraperDir;


    public static final String PROPSFILE =  "casm.jqm.scraper.props";
    @Parameter(names = {PROPSFILE}, description = "The props file containing postres and jqm params.")
    public String propsPath;

    public static final String QUEUE =  "casm.jqm.scraper.queueName";
    @Parameter(names = {QUEUE}, description = "The props file containing postres and jqm params.")
    public String queueNAme;


}
