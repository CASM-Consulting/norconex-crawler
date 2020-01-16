package uk.ac.susx.tag.norconex;

import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.impl.mvstore.MVStoreCrawlDataStore;
import com.norconex.collector.core.data.store.impl.mvstore.MVStoreCrawlDataStoreFactory;
import com.norconex.collector.http.HttpCollectorConfig;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.data.HttpCrawlData;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class MVStoreTest {


    @Test
    public void MVStoreTest() {

        String backup = "/Users/jp242/Documents/Projects/ACLED/Index-reduction/thejakartapost.com/crawlstore/mvstore/mvstore";
        File before = new File(backup);
        System.out.println("Before: " + before.length());

//        String mvstore = "/Users/jp242/Documents/Projects/ACLED/mvstoretest/libyaschannel.com/crawlstore/mvstore/libyaschannel.com_95_singleSeedCollector/mvstore";
        String mvstore = "/Users/jp242/Documents/Projects/ACLED/Index-reduction/thejakartapost.com/crawlstore/mvstore/thejakartapost.com_95_singleSeedCollector/mvstore";
        //        MVStore mv = new MVStore.Builder()
//                .fileName(mvstore)
//                .open();

        HttpCollectorConfig con = new HttpCollectorConfig();
        try {
            con.loadFromXML(new BufferedReader(new FileReader("resources/xml-config.xml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(mv.openMap);
        MVStore mv = MVStore.open(mvstore);
        final MVMap<String, ICrawlData> mapCached = mv.openMap("processedValid");
        final MVMap<String, ICrawlData> mapInCached = mv.openMap("processedInvalid");

        MVStoreCrawlDataStoreFactory mvfact = new MVStoreCrawlDataStoreFactory();
        HttpCrawlerConfig config = new HttpCrawlerConfig();
        mvfact.createCrawlDataStore(config, true);



        System.out.println("cache size: "+mv.compact(90,100));


//        HttpCrawlData data = (HttpCrawlData) mapCached.get("https://libyaschannel.com/tag/%D8%A7%D9%81%D8%B7%D8%A7%D8%B1/");
//        Arrays.stream(data.getReferencedUrls()).forEach(System.out::println);

        for(Map.Entry<String, ICrawlData> data : mapCached.entrySet()) {
            HttpCrawlData urlData = (HttpCrawlData) data.getValue();
            urlData.setReferencedUrls(new String[0]);
            urlData.setRedirectTrail(new String[0]);
            mapCached.put(data.getKey(),urlData);
        }
        for(Map.Entry<String, ICrawlData> data : mapInCached.entrySet()) {
            HttpCrawlData urlData = (HttpCrawlData) data.getValue();
            urlData.setReferencedUrls(new String[0]);
            urlData.setRedirectTrail(new String[0]);
            mapCached.put(data.getKey(),urlData);
        }
//        mapCached.clear();

        System.out.println(mapCached.size());
        mv.compactRewriteFully();
        mv.setVersionsToKeep(2);
        mv.setReuseSpace(true);

        mv.compactMoveChunks();

//        mv.compact(95,10);

        mv.commit();


        mv.close();
        File after = new File(mvstore);
        System.out.println("After: " + after.length());


    }
}
