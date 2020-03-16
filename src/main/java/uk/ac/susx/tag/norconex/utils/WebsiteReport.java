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

    public static boolean validate(String url) {
//        final String protocolURL = WebsiteAnalysis.addHttpProtocol(url,true);
        return UrlValidator.getInstance().isValid(url);
    }

    public void inValidURLReport(String url) {
        validURL = false;
        successfulPing = false;
        httpCode = -1;
        canCrawl = false;
        hasRobots = false;
        hasSitemap = false;
        LOG.error("The provided URL - " + url + " - is poorly formed.");
    }

    public static HttpURLConnection ensureConnection(String original) throws IOException {

        String httpsURL = WebsiteAnalysis.addHttpProtocol(original, true);
        if (!UrlValidator.getInstance().isValid(httpsURL)) {
            throw new MalformedURLException(httpsURL);
        }
        URL url = new URL(httpsURL);
        HttpURLConnection connection = null;
        try {
            connection = establishConnection(url);
        } catch (IOException e) {
            // If https fails then fallback to http and try again
            String httpURL = WebsiteAnalysis.addHttpProtocol(httpsURL, false);
            url = new URL(httpURL);
            connection = establishConnection(url);
        }
        return connection;
    }

    public void buildReport(String url) throws IOException {
        HttpURLConnection connection = null;
        HttpURLConnection robConnection = null;
        HttpURLConnection sitemap = null;
        try {
            connection = ensureConnection(url);

            URL parsedURL = connection.getURL();

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
                robConnection = establishConnection(new URL(hostId + "/robots.txt"));

                if(robConnection.getResponseCode() != 404) {
                    BaseRobotRules rules = getRobotRules(hostId,robConnection);
                    canCrawl = rules.isAllowed(parsedURL.toString());
                }
                else {
                    hasRobots = false;
                }
                robConnection.disconnect();

                // resolve sitemap
                sitemap = establishConnection(new URL(hostId + "/sitemap.xml"));
                if(sitemap.getResponseCode() != 200) {
                    hasSitemap = false;
                }
                sitemap.disconnect();
            }

        } catch (IOException e) {
            inValidURLReport(url);
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
            if(robConnection != null) {
                robConnection.disconnect();
            }
            if(sitemap != null) {
                sitemap.disconnect();
            }
        }

    }
}
