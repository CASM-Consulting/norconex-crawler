package uk.ac.susx.tag.norconex.utils;

import crawlercommons.robots.BaseRobotRules;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static uk.ac.susx.tag.norconex.utils.WebsiteAnalysis.*;

public class WebsiteReport {

    private static final Logger LOG = LoggerFactory.getLogger(WebsiteReport.class);

    private URL url;
    public boolean validURL = true;
    public boolean successfulPing = true;
    public boolean hasRobots = true;
    public boolean hasSitemap = false;
    public boolean canCrawl = true;
    public int httpCode;
    public String httpMessage;

    public WebsiteReport(String url) {
        if(UrlValidator.getInstance().isValid(url)) {
            try {
                this.url = new URL(url);
                try {
                    buildReport();
                } catch (IOException e) {
                    LOG.error("Failed when attempting to build statics on provided URL - " + url +  " - " + e.getMessage());
                }
            } catch (MalformedURLException e) {
                inValidURLReport();
            }
        } else {
            inValidURLReport();
        }

    }

    public void inValidURLReport() {
        validURL = false;
        successfulPing = false;
        httpCode = -1;
        canCrawl = false;
        LOG.error("The provided URL - " + url + " - is poorly formed.");
    }

    private void buildReport() throws IOException {


        HttpURLConnection connection = establishConnection(url);

        // Basic return status
        httpCode = connection.getResponseCode();
        httpMessage = connection.getResponseMessage();

        // Successful connection?
        if (httpCode != 200) {

            successfulPing = false;
            canCrawl = false;
            connection.disconnect();

        } else {

            // Resolve site configuration
            connection.disconnect();

            String hostId = getHostID(url);
            HttpURLConnection robConnection = establishConnection(new URL(hostId + "/robots.txt"));

            if(robConnection.getResponseCode() != 404) {
                BaseRobotRules rules = getRobotRules(hostId,robConnection);
                canCrawl = rules.isAllowed(this.url.toString());
            }
            else {
                hasRobots = false;
            }
            robConnection.disconnect();

            // resolve sitemap
            HttpURLConnection sitemap = establishConnection(new URL(hostId + "/sitemap.xml"));
            if(sitemap.getResponseCode() != 200) {
                hasSitemap = false;
            }
            sitemap.disconnect();
        }

    }
}
