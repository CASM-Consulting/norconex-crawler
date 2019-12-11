package uk.ac.susx.tag.norconex.crawlstore;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompactCrawlDatabases {

    protected static final Logger logger = LoggerFactory.getLogger(CompactCrawlDatabases.class);

    public boolean compactChunks(Path path) {

//        String mvstore = "/Users/jp242/Documents/Projects/JQM-Crawling/crawl-databases/greaterkashmir.com/crawlstore/mvstore/greaterkashmir.com_95_singleSeedCollector/mvstore";
        MVStore mv = new MVStore.Builder()
                .fileName(path.toString())
                .open();
        boolean success = mv.compactMoveChunks();
        mv.commit();
        mv.close();
        return success;

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
