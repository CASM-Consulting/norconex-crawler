package uk.ac.susx.tag.norconex.jobqueuemanager;

// jcommander imports
import com.beust.jcommander.JCommander;

// norconex imports
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.doc.HttpMetadata;
import com.norconex.collector.http.processor.IHttpDocumentProcessor;
import com.norconex.committer.sql.SQLCommitter;

// java logs
import com.norconex.importer.ImporterConfig;
import com.norconex.importer.handler.tagger.impl.KeepOnlyTagger;
import com.norconex.importer.parser.GenericDocumentParserFactory;
import org.apache.http.client.HttpClient;
import org.apache.regexp.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// java imports
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import uk.ac.susx.tag.norconex.utils.Utils;

/**
 * Standard crawler which allows crawled data to be pumped straight into the database
 */
public class Straight2DBCollector {

    protected static final Logger logger = LoggerFactory.getLogger(Straight2DBCollector.class);

    public static final String USER = "casm.jqm.crawling.user";
    public static final String TABLE = "casm.jqm.crawling.table";
    public static final String URL = "casm.jqm.crawling.url";
    public static final String DRIVERCLS = "casm.jqm.crawling.dbClass";
    public static final String DRIVERPATH = "casm.jqm.crawling.dbPath";

    public static final String CONTENT = "casmhtml";
    public static final String REFERENCE = "casmurl";

    public void runCollector(CrawlerArguments args) throws URISyntaxException {
        Properties props = Utils.getProperties(args.crawldbProps);
        SingleSeedCollector collector = new SingleSeedCollector(args.userAgent,new File(args.crawldb), Utils.getDomain(args.seeds.get(0)),
                args.depth, args.urlFilter,args.threadsPerSeed,args.ignoreRobots,
                args.ignoreSitemap, args.polite,
                args.seeds.get(0));

        HttpCrawlerConfig config = collector.getConfiguration();

        SQLCommitter committer = buildCommiter(props);
        committer.setQueueDir(Paths.get(args.crawldb,Utils.getDomain(args.seeds.get(0)), "commitqueue").toAbsolutePath().toString());
        config.setCommitter(committer);

        ImporterConfig ic = new ImporterConfig();
        ic.setPostParseHandlers(buildKeepOnlyTagger());
        config.setImporterConfig(ic);

        collector.setConfiguration(config);
        collector.start();
        committer.close();
    }

    private KeepOnlyTagger buildKeepOnlyTagger() {
        KeepOnlyTagger kop = new KeepOnlyTagger();
//        kop.addField(HttpMetadata.DOC_IMPORTED_DATE);
        //  date, depth and seen before info
//        kop.addField(HttpMetadata.DOC_REFERENCE);
//        kop.addField("ACLEDHTML");
        return kop;
    }

//    /**
//     * Wrapper class around @SQLCommiter for easy configuration.
//     */
//    public class PropertiesBasedDBCommiter extends SQLCommitter {
//
//        protected final Logger logger = LoggerFactory.getLogger(Straight2DBCollector.class);
//
//        public PropertiesBasedDBCommiter(Properties props) {
//
//            this.setConnectionUrl(props.getProperty(URL)); // Needs to include
//            this.setDriverClass(props.getProperty(DRIVERCLS));
//            this.setDriverPath(props.getProperty(DRIVERPATH));
//            this.setTableName(props.getProperty(TABLE));
//            this.setUsername(props.getProperty(USER));
//            this.setTargetContentField(CONTENT);
//            this.setTargetReferenceField(REFERENCE);
//            this.setKeepSourceContentField(true);
//            this.setKeepSourceReferenceField(true);
//            this.setMaxRetries(10);
//            this.setCommitBatchSize(10);
//
//        }
//    }

    public SQLCommitter buildCommiter(Properties props) {
        SQLCommitter committer = new SQLCommitter();
        committer.setConnectionUrl(props.getProperty(URL)); // Needs to include
        committer.setDriverClass(props.getProperty(DRIVERCLS));
        committer.setDriverPath(props.getProperty(DRIVERPATH));
        committer.setTableName(props.getProperty(TABLE));
        committer.setUsername(props.getProperty(USER));
        committer.setTargetContentField(CONTENT);
        committer.setTargetReferenceField(REFERENCE);
//        committer.setSourceContentField("content");
//        committer.setSourceReferenceField("reference");
//        committer.setKeepSourceContentField(true);
//        committer.setKeepSourceReferenceField(true);
        committer.setFixFieldValues(true);
        committer.setFixFieldNames(true);
        committer.setKeepSourceContentField(false);
        committer.setKeepSourceReferenceField(false);
        committer.setMaxRetries(10);
        committer.setCommitBatchSize(10);
        committer.setQueueSize(10);
//        committer.setFixFieldNames(true);
//        committer.setFixFieldValues(true);
//        committer.setCreateFieldSQL(CONTENT);
//        committer.setCreateTableSQL("crawlerhtml");

        return committer;
    }

    public static void main(String[] args) {

        List<String> splitArgs = new ArrayList<>();
        for(String arg : args){
            splitArgs.addAll(Arrays.asList(arg.split("\\s+")));
        }

        String[] corrArgs = splitArgs.toArray(new String[splitArgs.size()]);

        CrawlerArguments crawlerArguments = new CrawlerArguments();
        JCommander.newBuilder()
                .addObject(crawlerArguments)
                .build()
                .parse(corrArgs);

        Straight2DBCollector collector = new Straight2DBCollector();
        try {
            collector.runCollector(crawlerArguments);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Crawler failed to start - " + e.getMessage());
        }

    }

}
