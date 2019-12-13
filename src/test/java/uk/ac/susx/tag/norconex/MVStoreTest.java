package uk.ac.susx.tag.norconex;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.Test;

import java.io.File;
import java.util.Map;

public class MVStoreTest {


    @Test
    public void MVStoreTest() {

        String backup = "/Users/jp242/Documents/Projects/JQM-Crawling/crawl-databases/tag-10/taglaboratory.org/crawlstore/mvstore/mvstore";
        File before = new File(backup);
        System.out.println("Before: " + before.length());

        String mvstore = "/Users/jp242/Documents/Projects/JQM-Crawling/crawl-databases/tag-10/taglaboratory.org/crawlstore/mvstore/taglaboratory.org_95_singleSeedCollector/mvstore";
        MVStore mv = new MVStore.Builder()
                .fileName(mvstore)
                .open();
//        mv.compactRewriteFully();
        mv.compactMoveChunks();
        mv.commit();
        mv.close();


        File after = new File(mvstore);
        System.out.println("After: " + after.length());


    }
}
