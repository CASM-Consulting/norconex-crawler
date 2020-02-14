package uk.ac.susx.tag.norconex.database;

// java imports
import com.norconex.importer.doc.ImporterDocument;
import org.apache.commons.io.FileUtils;

// h2 imports
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

// java imports
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.norconex.collector.core.data.store.CrawlDataStoreException;

public class ConcurrentContentHashStore {

    private MVStore store;

    private final MVMap<String, String> contentHashMap;
    private static final String STORENAME = "contentHashStore";
    private static final String MAPNAME = "contentHashMap";

    public ConcurrentContentHashStore(Path storeLocation){

        try {
            FileUtils.forceMkdir(storeLocation.toFile());
        } catch (IOException e) {
            throw new CrawlDataStoreException(
                    "Cannot create crawl data store directory: " + storeLocation, e);
        }

        store = MVStore.open(Paths.get(storeLocation.toAbsolutePath().toString(), STORENAME).toString());
        contentHashMap = store.openMap(MAPNAME);

    }

    public synchronized boolean containsContentHash(String hash) {
        return contentHashMap.containsKey(hash);
    }

    /**
     * @param hash the checksum hash of the content
     * @param data the document - to disambiguate the two strings and provide access to more that just URL/reference
     */
    public synchronized void addContentHash(String hash, ImporterDocument data) {
        contentHashMap.put(hash,data.getReference());
        store.commit();
    }

    public void close() {
        if(!store.isClosed()) {
            store.commit();
            store.close();
        }
    }

}
