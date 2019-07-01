package uk.ac.susx.tag.norconex.document;

// boilerpipe imports
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.CommonExtractors;

// javax imports
import javax.xml.stream.XMLStreamException;

// java imports
import java.io.IOException;
import java.io.StringWriter;

import com.norconex.collector.core.checksum.AbstractDocumentChecksummer;
import com.norconex.collector.core.checksum.ChecksumUtil;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;
import com.norconex.importer.doc.ImporterDocument;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;

public class ArticleExtractorChecksum extends AbstractDocumentChecksummer {


    @Override
    protected String doCreateDocumentChecksum(ImporterDocument document) {


        document.getContent().rewind();

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
            return ChecksumUtil.checksumMD5(article);

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
