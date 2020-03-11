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
        String httpsurl = WebsiteAnalysis.addHttpProtocol(httpurl, true);
        try {
            URL parsedUrl = new URL(httpsurl);
            System.out.println(httpsurl);
            Assert.assertEquals(parsedUrl.getProtocol(), "https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }
    }

    @Test
    public void testAddHttpProtocolReplacement2() {
        String httpsurl = "https://www.taglaboratory.org/";
        String httpurl = WebsiteAnalysis.addHttpProtocol(httpsurl,false);
        try {
            URL parsedUrl = new URL(httpurl);
            System.out.println(httpurl);
            Assert.assertEquals(parsedUrl.getProtocol(),"http");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }
    }

    @Test
    public void testAddHttpProtocolReplacement3() {
        String noprotourl = "www.taglaboratory.org/";
        String noprotohttpsurl = WebsiteAnalysis.addHttpProtocol(noprotourl,true);
        try {
            URL parsedUrl = new URL(noprotohttpsurl);
            System.out.println(noprotohttpsurl);
            Assert.assertEquals(parsedUrl.getProtocol(),"https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }
    }

    @Test
    public void testAddHttpProtocolReplacement4() {
        String noweburl = "taglaboratory.org/";
        String nowebhttps = WebsiteAnalysis.addHttpProtocol(noweburl,true);
        try {
            URL parsedUrl = new URL(nowebhttps);
            System.out.println(nowebhttps);
            Assert.assertEquals(parsedUrl.getProtocol(),"https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }
    }

    @Test
    public void testAddHttpProtocolReplacement5() {
        String notWebUrl = "fttp://taglaboratory.org/";
        String nothttps = WebsiteAnalysis.addHttpProtocol(notWebUrl,true);
        try {
            URL parsedUrl = new URL(nothttps);
            System.out.println(nothttps);
            Assert.assertEquals(parsedUrl.getProtocol(),"https");
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            Assert.fail();
        }
    }

    @Test
    public void testAddHttpProtocolReplacement6() {
        //SW: I think we can reject this as malformed
        String messyUrl = "fttcsp:taglaboratory.org/";
        String messyhttps = WebsiteAnalysis.addHttpProtocol(messyUrl,true);
        try {
            URL parsedUrl = new URL(messyhttps);
            System.err.println(messyhttps);
            Assert.fail();
        } catch (MalformedURLException e) {
            System.out.println(messyUrl + " rejected");
//            Assert.fail();
        }
    }

}
