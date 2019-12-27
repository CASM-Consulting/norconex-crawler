package uk.ac.susx.tag.norconex.crawlstore;

import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.http.data.HttpCrawlData;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompactCrawlDatabases {

    protected static final Logger logger = LoggerFactory.getLogger(CompactCrawlDatabases.class);

    public boolean compactChunks(Path path) {

//        String mvstore = "tests/crawldb/taglaboratory.org/crawlstore/mvstore/taglaboratory.org_95_singleSeedCollector/mvstore";
        MVStore mv = new MVStore.Builder()
                .fileName(path.toString())
                .open();

        // compact the db
        boolean success = mv.compactMoveChunks();

        // Remove superfluous referral information
        final MVMap<String, ICrawlData> mapCached = mv.openMap("processedValid");
        final MVMap<String, ICrawlData> mapInCached = mv.openMap("processedInvalid");
        removeReferrals(mapCached);
        removeReferrals(mapInCached);
        mv.commit();

        mv.close();
        return success;

    }

    private void removeReferrals(MVMap<String, ICrawlData> crawlstore) {
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
