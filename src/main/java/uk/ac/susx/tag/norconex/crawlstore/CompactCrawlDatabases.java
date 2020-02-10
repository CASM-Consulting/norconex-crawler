package uk.ac.susx.tag.norconex.crawlstore;

//norconex
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.http.data.HttpCrawlData;

// h2 imports
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Simple program that allows users to specify a single crawldb of directory containing many and remove all
 * stored urls discovered on a page.
 */
public class CompactCrawlDatabases {

    protected static final Logger logger = LoggerFactory.getLogger(CompactCrawlDatabases.class);

    public boolean compactChunks(Path path) {

        try {

            MVStore mv = new MVStore.Builder()
                    .fileName(path.toString())
                    .open();

            // compact the db
            mv.compactMoveChunks();

            // Remove superfluous referral information
            final MVMap<String, ICrawlData> mapCached = mv.openMap("processedValid");
            final MVMap<String, ICrawlData> mapInCached = mv.openMap("processedInvalid");
            removeReferencedURLSFromCrawlDBIndex(mapCached);
            removeReferencedURLSFromCrawlDBIndex(mapInCached);

            mv.commit();

            mv.close();
        } catch (Exception e) {
            logger.error("ERROR: Failed - " + path.toString());
            return false;
        }

        return true;

    }

    /**
     * Deletes all referenced URLs from a crawldb
     * @param crawlstore
     */
    private void removeReferencedURLSFromCrawlDBIndex(MVMap<String, ICrawlData> crawlstore) {
        for(Map.Entry<String, ICrawlData> data : crawlstore.entrySet()) {
            HttpCrawlData urlData = (HttpCrawlData) data.getValue();
            urlData.setReferencedUrls(new String[0]);
            urlData.setRedirectTrail(new String[0]);
            crawlstore.put(data.getKey(),urlData);
        }
    }

    public void walkAndCompactDatabases(Path crawlDatabases) {

        try(Stream<Path> walk = Files.walk(crawlDatabases)) {
            walk.filter(path -> path.getFileName().toString().equals("mvstore"))
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(db -> compactChunks(db));

        } catch (IOException e) {
            logger.error("ERROR: failed when attempting to walk filesystem");
        }

    }

    public static void main(String[] args) {
        CompactCrawlDatabases ccd = new CompactCrawlDatabases();
        ccd.walkAndCompactDatabases(Paths.get(args[0]));
    }

}
