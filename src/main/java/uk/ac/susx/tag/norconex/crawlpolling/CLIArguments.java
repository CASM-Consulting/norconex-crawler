package uk.ac.susx.tag.norconex.crawlpolling;

import com.beust.jcommander.Parameter;

/**
 * Inner class used to hold command line arguments
 */
public class CLIArguments {

    @Parameter(names = {QueuedPollingManager.CACHE}, description = "The location of to cache jobs and ids.")
    public String cache;

    @Parameter(names = {QueuedPollingManager.RESTART}, description = "Whether or not to restart the monitoring service.",
            arity = -1)
    public boolean restart;

    @Parameter(names = {QueuedPollingManager.PROPS}, description = "The location of the manager properties.")
    public String properties;

    @Parameter(names = {QueuedPollingManager.KILLALL}, description = "Used to flag that all jobs should be killed.",
            arity = -1)
    public boolean kill;

}
