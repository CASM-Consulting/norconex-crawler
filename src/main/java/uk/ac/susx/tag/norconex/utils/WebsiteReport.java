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

    public boolean validURL = true;
    public boolean successfulPing = true;
    public boolean hasRobots = true;
    public boolean hasSitemap = false;
    public boolean canCrawl = true;
    public int httpCode;
    public String httpMessage;

    public boolean validate(String url) {
        final String protocolURL = WebsiteAnalysis.addHttpProtocol(url,true);
        return UrlValidator.getInstance().isValid(protocolURL);
    }

    public void inValidURLReport(String url) {
        validURL = false;
        successfulPing = false;
        httpCode = -1;
        canCrawl = false;
        LOG.error("The provided URL - " + url + " - is poorly formed.");
    }

    private void buildReport(String url) throws IOException {

        final String protocolURL = WebsiteAnalysis.addHttpProtocol(url,true);
        if(!validate(protocolURL)) {
            inValidURLReport(protocolURL);
            return;
        }
        try {
            URL parsedURL = new URL(protocolURL);
            HttpURLConnection connection = establishConnection(parsedURL);

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

                String hostId = getHostID(parsedURL);
                HttpURLConnection robConnection = establishConnection(new URL(hostId + "/robots.txt"));

                if(robConnection.getResponseCode() != 404) {
                    BaseRobotRules rules = getRobotRules(hostId,robConnection);
                    canCrawl = rules.isAllowed(parsedURL.toString());
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

        } catch (MalformedURLException e) {
            inValidURLReport(protocolURL);
        }

    }
}
