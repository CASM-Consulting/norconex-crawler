package uk.ac.susx.tag.norconex.database;

import com.norconex.committer.sql.SQLCommitter;

import java.util.Properties;

/**
 * Wrapper class around @SQLCommiter for easy configuration.
 */
public class PropertiesBasedDBCommiter extends SQLCommitter {

    public static final String USER =   "casm.jqm.crawling.user";
    public static final String TABLE =  "casm.jqm.crawling.table";
    public static final String URL =    "casm.jqm.crawling.url";
    public static final String DRIVERCLS ="casm.jqm.crawling.dbClass";
    public static final String DRIVERPATH="casm.jqm.crawling.dbPath";

    public static final String CONTENT = "casm.html";
    public static  final String REFERENCE = "casm.url";

    public PropertiesBasedDBCommiter(Properties props) {
        this.setConnectionUrl(props.getProperty(URL)); // Needs to include
        this.setDriverClass(props.getProperty(DRIVERCLS));
        this.setDriverPath(props.getProperty(DRIVERPATH));
        this.setTableName(props.getProperty(TABLE));
        this.setUsername(props.getProperty(USER));
        this.setTargetContentField(props.getProperty(CONTENT));
        this.setTargetReferenceField(props.getProperty(REFERENCE));
        this.setKeepSourceContentField(false);
        this.setKeepSourceReferenceField(false);
        this.setMaxRetries(10);
        this.setCommitBatchSize(1000);
    }

}
