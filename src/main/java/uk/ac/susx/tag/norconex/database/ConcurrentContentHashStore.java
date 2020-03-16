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

public class ConcurrentContentHashStore implements AutoCloseable {

    private MVStore store;

    private final Path storeLocation;

    private MVMap<String, String> contentHashMap;
    private static final String STORENAME = "contentHashStore";
    private static final String MAPNAME = "contentHashMap";

    public ConcurrentContentHashStore(Path storeLocation){

        try {
            FileUtils.forceMkdir(storeLocation.toFile());
        } catch (IOException e) {
            throw new CrawlDataStoreException(
                    "Cannot create crawl data store directory: " + storeLocation, e);
        }

        this.storeLocation = storeLocation;
    }

    public synchronized boolean containsContentHash(String hash) {
        try (
            ConcurrentContentHashStore cchs = this.open()
        ) {
            return contentHashMap.containsKey(hash);
        }
    }

    /**
     * @param hash the checksum hash of the content
     * @param data the document - to disambiguate the two strings and provide access to more that just URL/reference
     */
    public synchronized void addContentHash(String hash, ImporterDocument data) {
        try (
            ConcurrentContentHashStore cchs = this.open()
        ) {
            contentHashMap.put(hash,data.getReference());
            store.commit();
        }
    }

    public synchronized ConcurrentContentHashStore open() {
        if(store == null || store.isClosed()) {
            store = MVStore.open(Paths.get(storeLocation.toAbsolutePath().toString(), STORENAME).toString());
            contentHashMap = store.openMap(MAPNAME);
        }
        return this;
    }

    @Override
    public void close() {
        if(store != null && !store.isClosed()) {
            store.commit();
            store.close();
        }
    }

}
