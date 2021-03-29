package uk.ac.susx.tag.norconex;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.tag.norconex.utils.WebsiteReport;

import java.io.IOException;

public class TestWebsiteReporter {

    @Test
    public void testWebSiteReporter() {
        String url = "www.taglaboratory.org";
        WebsiteReport report = new WebsiteReport();
        try {
            report.buildReport(url);
            System.out.println(report);
        } catch (IOException e) {
            Assert.fail();
        }
    }


}
