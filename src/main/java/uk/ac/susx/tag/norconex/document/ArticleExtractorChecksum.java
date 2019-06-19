package uk.ac.susx.tag.norconex.document;

import com.norconex.collector.core.checksum.AbstractDocumentChecksummer;
import com.norconex.collector.core.checksum.ChecksumUtil;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;
import com.norconex.importer.doc.ImporterDocument;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;

public class ArticleExtractorChecksum extends AbstractDocumentChecksummer {


    @Override
    protected String doCreateDocumentChecksum(ImporterDocument document) {


        document.getContent().rewind();

        StringWriter sw = new StringWriter();
        document.getContent().rewind();
        try {
            IOUtils.copy(document.getContent(), sw, document.getContentEncoding());
        } catch (IOException e) {
            throw new RuntimeException("ERROR: Failed to retrieve web content for url: " + document.getReference());
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
