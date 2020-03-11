package uk.ac.susx.tag.norconex.utils;

import com.google.common.io.ByteStreams;
import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains some static methods for scrutinising a site and provides a @Report class which builds some shallow statistics
 * regarding the given site.
 */
public class WebsiteAnalysis {

    private static final Logger LOG = LoggerFactory.getLogger(WebsiteAnalysis.class);

    public static final String USER_AGENT = "casmconsulting.co.uk";

    private static final Pattern PROTOCOL = Pattern.compile(".*:[/]{2}", Pattern.CASE_INSENSITIVE);

    public static BaseRobotRules getRobotRules(String hostId, HttpURLConnection robotsConnection) throws IOException {
        SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
        BaseRobotRules rules = robotParser.parseContent(hostId, ByteStreams.toByteArray((InputStream) robotsConnection.getContent()),
                "text/plain", USER_AGENT);
        return rules;
    }

    public static String addHttpProtocol(String url, boolean https) {

        Matcher matcher = PROTOCOL.matcher(url.toLowerCase());

        if(matcher.find()) {
            // get rid of any previous or malformed protocol
            url = matcher.replaceFirst("");
        }
        url =  (https) ? "https://" + url : "http://" + url;

        return url;
    }

    public static String getHostID(URL url) {
        // Check robots and sitemap
        return url.getProtocol() + "://" + url.getHost()
                + ((url.getPort() > -1) ? ":" + url.getPort() : "");
    }

    public static HttpURLConnection establishConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return connection;
    }



}
