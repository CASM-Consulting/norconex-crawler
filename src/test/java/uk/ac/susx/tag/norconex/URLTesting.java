package uk.ac.susx.tag.norconex;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.tag.norconex.utils.WebsiteAnalysis;

import java.net.MalformedURLException;
import java.net.URL;

public class URLTesting {

    @Test
    public void testAddHttpProtocolReplacement() {
        String httpurl = "http://www.taglaboratory.org/";
        String httpsurl = WebsiteAnalysis.addHttpProtocol(httpurl,true);
        try {
            URL parsedUrl = new URL(httpsurl);
            System.err.println(httpsurl);
            Assert.assertEquals(parsedUrl.getProtocol(),"https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }

        httpsurl = "https://www.taglaboratory.org/";
        httpurl = WebsiteAnalysis.addHttpProtocol(httpsurl,false);
        try {
            URL parsedUrl = new URL(httpurl);
            System.err.println(httpurl);
            Assert.assertEquals(parsedUrl.getProtocol(),"http");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }

        String noprotourl = "www.taglaboratory.org/";
        String noprotohttpsurl = WebsiteAnalysis.addHttpProtocol(noprotourl,true);
        try {
            URL parsedUrl = new URL(noprotohttpsurl);
            System.err.println(noprotohttpsurl);
            Assert.assertEquals(parsedUrl.getProtocol(),"https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }

        String noweburl = "taglaboratory.org/";
        String nowebhttps = WebsiteAnalysis.addHttpProtocol(noweburl,true);
        try {
            URL parsedUrl = new URL(nowebhttps);
            System.err.println(nowebhttps);
            Assert.assertEquals(parsedUrl.getProtocol(),"https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }

        String notWebUrl = "fttp://taglaboratory.org/";
        String nothttps = WebsiteAnalysis.addHttpProtocol(notWebUrl,true);
        try {
            URL parsedUrl = new URL(nothttps);
            System.err.println(nothttps);
            Assert.assertEquals(parsedUrl.getProtocol(),"https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }

        String messyUrl = "fttcsp:taglaboratory.org/";
        String messyhttps = WebsiteAnalysis.addHttpProtocol(messyUrl,true);
        try {
            URL parsedUrl = new URL(messyhttps);
            System.err.println(messyhttps);
            Assert.assertEquals(parsedUrl.getProtocol(),"https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }
    }

}
