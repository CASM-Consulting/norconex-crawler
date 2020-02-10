package uk.ac.susx.tag.norconex.document;

// boilerpipe imports
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.CommonExtractors;

// javax imports
import javax.xml.stream.XMLStreamException;

// java imports
import java.io.IOException;
import java.io.StringWriter;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.checksum.AbstractDocumentChecksummer;
import com.norconex.collector.core.checksum.ChecksumUtil;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;
import com.norconex.importer.doc.ImporterDocument;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;


public class ArticleExtractorChecksum extends AbstractDocumentChecksummer {

    protected static final Logger logger = LoggerFactory.getLogger(ArticleExtractorChecksum.class);
    public static final String CHECKSUM = "casm.co.uk.checkum";

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

        try {
            final String article = CommonExtractors.ARTICLE_EXTRACTOR.getText(html);
            String checksum = ChecksumUtil.checksumMD5(article);
            //TODO: add the checkum tp metadata for update change
            return checksum;

        } catch (BoilerpipeProcessingException e) {
            throw new RuntimeException("ERROR: Failed to extract article from url: " + document.getReference());
        }

    }

    @Override
    protected void loadChecksummerFromXML(XMLConfiguration xml) {

    }

    @Override
    protected void saveChecksummerToXML(EnhancedXMLStreamWriter writer) throws XMLStreamException {

    }
}
