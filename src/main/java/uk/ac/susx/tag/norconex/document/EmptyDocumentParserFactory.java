package uk.ac.susx.tag.norconex.document;

// norconex imports
import com.norconex.commons.lang.file.ContentType;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.parser.DocumentParserException;
import com.norconex.importer.parser.IDocumentParser;
import com.norconex.importer.parser.IDocumentParserFactory;

// java imports
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

// Used to override default Norconex behaviour that
public class EmptyDocumentParserFactory implements IDocumentParserFactory {

    @Override
    public IDocumentParser getParser(String documentReference, ContentType contentType) {
        return new EmptyDocumentParser();
    }

    public class EmptyDocumentParser implements IDocumentParser {

        @Override
        public List<ImporterDocument> parseDocument(ImporterDocument doc, Writer output) throws DocumentParserException {
            return Arrays.asList(doc);
        }
    }
}