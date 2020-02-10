package uk.ac.susx.tag.norconex.document;

import com.google.gson.Gson;
import com.norconex.collector.core.checksum.AbstractDocumentChecksummer;
import com.norconex.collector.core.checksum.ChecksumUtil;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.doc.HttpMetadata;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;
import com.norconex.importer.doc.ImporterDocument;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.CommonExtractors;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.database.ConcurrentContentHashStore;
import uk.ac.susx.tag.norconex.jobqueuemanager.CrawlerArguments;
import uk.ac.susx.tag.norconex.scraping.GeneralSplitterFactory;
import uk.ac.susx.tag.norconex.scraping.IForumSplitter;
import uk.ac.susx.tag.norconex.scraping.Post;
import uk.ac.susx.tag.norconex.utils.ScraperNotFoundException;
import uk.ac.susx.tag.norconex.utils.Utils;
import uk.ac.susx.tag.norconex.utils.WebPage;

import javax.rmi.CORBA.Util;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WebScraperChecksum extends AbstractDocumentChecksummer {

    protected static final Logger logger = LoggerFactory.getLogger(WebScraperChecksum.class);

    public static final String SCRAPEDARTICLE = "scraped.article";
    public static final String SCRAPEDATE = "scraped.date";
    public static final String SCRAPEDTITLE = "scraped.title";

    public static final String article = "field.name/article";
    public static final String title = "field.name/title";
    public static final String date = "field.name/date";

    public static final String metaDATE = "date";
    public static final String metaTITLE = "title";
    public static final String metaARTICLE = "article";

    public static Map<String, GeneralSplitterFactory> scrapers = new HashMap<>();

    public static ConcurrentContentHashStore contentHashes;

    // Used if you wish the pre-processor to only be repsonsible for a single scraper.
    public static GeneralSplitterFactory scraper;

    private final Gson gson;

    public WebScraperChecksum(Path scraperLocation, ConcurrentContentHashStore contentHashes) {

        this.contentHashes = contentHashes;

        gson = new Gson();
        try {
            if(Files.isDirectory(scraperLocation) && !Files.exists(Paths.get(scraperLocation.toAbsolutePath().toString(),"job.json"))) {
                logger.info("INFO: Provided a directory for scraper location - attempting to load all scrapers it contains.");
                scrapers = Utils.initScrapers(scraperLocation);
            }
            else {
                logger.info("INFO: Provided a specific crawler to load and scrape pages with.");
                Utils.initScraper(scraperLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed: when attempting to initialise web scraper(s): " + e.getMessage());
        }
    }

    @Override
    protected String doCreateDocumentChecksum(ImporterDocument document) {

        StringWriter sw = new StringWriter();
        document.getContent().rewind();

        try {
            IOUtils.copy(document.getContent(), sw, document.getContentEncoding());
        } catch (IOException e) {
            throw new RuntimeException("ERROR: Failed to retrieve content for url: " + document.getReference());
        }

        final String html = sw.toString();

        String checksum = null;
        WebPage webPage = processDocument((HttpDocument) document);
        if(webPage != null && webPage.getArticle() != null && webPage.getArticle().length() > 0) {
            checksum = ChecksumUtil.checksumMD5(webPage.getArticle());

            // Check if that scraped content already exists - if not add it to the document for post-processing
            if(!contentHashes.containsContentHash(checksum)) {
                addScrapedContentToMetadata(webPage, (HttpDocument) document);
                logger.info("URL will not be sent for further processing as content has already been processed - " + document.getReference());
            }

        } else {
            try {
                checksum = ChecksumUtil.checksumMD5(generalScraper(html));
                contentHashes.addContentHash(checksum, document);
            } catch (BoilerpipeProcessingException e) {
                logger.error("Boilerpipe failed to process html for " + document.getReference() + " " + e.getMessage());
            }
        }

        return checksum;
    }

    public WebPage processDocument(HttpDocument doc) {

        if(Utils.isText(doc)) {

            final String url = doc.getReference();

            StringWriter sw = new StringWriter();
            doc.getContent().rewind();
            try {
                IOUtils.copy(doc.getContent(), sw, doc.getContentEncoding());
            } catch (IOException e) {
                throw new RuntimeException("ERROR: Failed to retrieve web content for url: " + url);
            }

            final String html = sw.toString();

            final String parent = doc.getMetadata().getString(HttpMetadata.COLLECTOR_REFERRER_REFERENCE);
            final int depth = doc.getMetadata().getInt(HttpMetadata.COLLECTOR_DEPTH);

            final WebPage webPage = new WebPage(url,html,parent,depth);
            try {
                scrape_page(webPage);
            } catch (ScraperNotFoundException e) {
                logger.warn("Scraper not found for article ");
            } catch (MalformedURLException e) {
                logger.warn("Malformed url exception");
            } catch (URISyntaxException e) {
                logger.warn("Malformed url exception");
            }
            return webPage;
        }
        return null;
    }

    public void addScrapedContentToMetadata(WebPage webPage, HttpDocument doc) {
        // Check content was scraped.
        if(webPage.getArticle() != null && webPage.getArticle().length() > 0) {
            // Check content does not already exist in the scraped db.
            List<String> pages = new ArrayList<>();
            doc.getMetadata().put(SCRAPEDARTICLE,Arrays.asList(webPage.getArticle()));
            if(webPage.getTitle() != null && webPage.getTitle().length() > 0) {
                doc.getMetadata().put(SCRAPEDTITLE, Arrays.asList(webPage.getTitle()));
            }
            if(webPage.getDate() != null && webPage.getDate().length() > 0) {
                doc.getMetadata().put(SCRAPEDATE, Arrays.asList(webPage.getDate()));
            }
        }
    }

    public WebPage scrape_page(WebPage page) throws ScraperNotFoundException, MalformedURLException, URISyntaxException {


        String domain = Utils.getDomain(page.getUrl());


        // If there is a factory set for this preprocessor use that else search for one via the page's domain of origin
        // Prefered functionality is to set a single preprocessor (more robust)
        GeneralSplitterFactory factory = scraper;

        if(factory == null) {
            factory = scrapers.get(domain.replaceAll("\\.",""));
            if(factory == null){
                logger.error("No scraper was found for the domain " + domain.replaceAll("\\.",""));
                throw new ScraperNotFoundException(domain);
            }
        }

        IForumSplitter splitter = factory.create();

        LinkedList<Post> newspages = splitter.split(Jsoup.parse(page.getHtml()));
        if(newspages.size() > 0) {
            Post newspage = newspages.get(0);

            if(newspage.containsKey(article) && newspage.get(article).get(0).length() > 0) {
                page.setArticle(newspage.get(article).get(0));
            }
            else {
                return page;
            }
            if (newspage.containsKey(title) && newspage.get(title).get(0).length() > 0){
                page.setTitle(newspage.get(title).get(0));
            }
            if(newspage.containsKey(date) && newspage.get(date).get(0).length() > 0){
                page.setDate(newspage.get(date).get(0));
            }
        }

        return page;

    }

    /**
     * Use this as a fallback if there is no scraper implementation
     * @param html
     * @return
     * @throws BoilerpipeProcessingException
     */
    private String generalScraper(String html) throws BoilerpipeProcessingException {
        return CommonExtractors.ARTICLE_EXTRACTOR.getText(html);
    }

    @Override
    protected void loadChecksummerFromXML(XMLConfiguration xml) {
        // Not needed
    }

    @Override
    protected void saveChecksummerToXML(EnhancedXMLStreamWriter writer) throws XMLStreamException {
        // Not needed
    }
}
