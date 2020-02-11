package uk.ac.susx.tag.norconex.document;

import com.norconex.collector.core.checksum.AbstractDocumentChecksummer;
import com.norconex.collector.core.checksum.ChecksumUtil;
import com.norconex.collector.core.doc.CollectorMetadata;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.doc.HttpMetadata;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;
import com.norconex.importer.doc.ImporterDocument;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.database.ConcurrentContentHashStore;
import uk.ac.susx.tag.norconex.utils.Utils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

public class WebScraperMetadataChecksum extends AbstractDocumentChecksummer {

    protected static final Logger logger = LoggerFactory.getLogger(WebScraperMetadataChecksum.class);

    public static ConcurrentContentHashStore contentHashes;

    public WebScraperMetadataChecksum(ConcurrentContentHashStore contentHashes, String field) {
        this.contentHashes = contentHashes;
        this.setTargetField(field);
        this.setKeep(true);
    }

    @Override
    protected String doCreateDocumentChecksum(ImporterDocument document) {

        String checksum = null;

        if(Utils.isText((HttpDocument) document)) {
            final String content = document.getMetadata().get(getTargetField()).get(0);
            if (content != null && content.length() > 0) {
                checksum = ChecksumUtil.checksumMD5(content);
                // Check if that scraped content already exists - if not add it to the document for post-processing
                if (!contentHashes.containsContentHash(checksum)) {
                    contentHashes.addContentHash(checksum, document);
                    logger.info("URL will be sent for further processing as content has not been seen before - " + document.getReference());
                } else {
                    setChecksumMetadata(checksum,(HttpDocument) document);
                    logger.info("Content has been seen before - will not be processed again:  " + document.getReference());
                }
            } else {
                try {

                    final String url = document.getReference();

                    StringWriter sw = new StringWriter();
                    document.getContent().rewind();
                    try {
                        IOUtils.copy(document.getContent(), sw, document.getContentEncoding());
                    } catch (IOException e) {
                        throw new RuntimeException("ERROR: Failed to retrieve web content for url: " + url);
                    }

                    final String html = sw.toString();
                    checksum = ChecksumUtil.checksumMD5(generalScraper(html));
                    contentHashes.addContentHash(checksum, document);
                } catch (BoilerpipeProcessingException e) {
                    logger.error("Boilerpipe failed to process html for " + document.getReference() + " " + e.getMessage());
                }
            }
        }
        return checksum;
    }


    /**
     * This is only called when a document content checksum has been seen before from another URL.
     */
    private void setChecksumMetadata(String checksum, HttpDocument document) {
        document.getMetadata().put(CollectorMetadata.COLLECTOR_CHECKSUM_DOC, Arrays.asList(checksum));
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
        // not needed
    }

    @Override
    protected void saveChecksummerToXML(EnhancedXMLStreamWriter writer) throws XMLStreamException {
        // not needed
    }
}
