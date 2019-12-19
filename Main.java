package eoe.code;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

  //public static String ZK = "ericks-mbp:2181";
  //public static String ZK = "localhost:2181";
  //public static String ZK = "ericks-mac-pro:2181";
  public static String COLLECTION = "test";
  public static String URL = "http://localhost:8981/solr/test";

  static AtomicInteger grandTotal = new AtomicInteger();
  static AtomicInteger idAtomic = new AtomicInteger();
  static AtomicBoolean shouldStop = new AtomicBoolean(false);
  static DecimalFormat decFormat = new DecimalFormat("###,###");

  static Words words = null;

  public static void main(String[] args) {
    try {
      words = new Words();
      long start = System.currentTimeMillis();
      Main m = new Main();
      m.doIt();
//      m.querySome();
      System.out.println("Entire run took (ms): " + decFormat.format(System.currentTimeMillis() - start));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  //  void querySome() throws IOException, SolrServerException {
//    try (CloudSolrClient client = new CloudSolrClient.Builder().withZkHost(Main.ZK).build()) {
//      Words words = new Words();
//      client.setDefaultCollection(Main.COLLECTION);
//      int zeros = 0;
//      StringBuilder sb = new StringBuilder();
//      for (int idx = 0; idx < 32; idx++) {
//        sb.setLength(0);
//
//        String w1 = words.getAWord();
//        String w2 = words.getAWord();
//        if (w1.compareToIgnoreCase(w2) > 0) {
//          String tmp = w2;
//          w2 = w1;
//          w1 = tmp;
//        }
//        sb.append("<lst><str name=\"q\">").append("text:[" + w1 + " TO " + w2 + "]").append("</str>");
//        SolrQuery q = new SolrQuery("text:[" + w1 + " TO " + w2 + "]");
//        w1 = words.getAWord();
//        w2 = words.getAWord();
//        if (w1.compareToIgnoreCase(w2) > 0) {
//          String tmp = w1;
//          w2 = w1;
//          w1 = tmp;
//        }
//        sb.append("<str name=\"fq\">").append("text:[" + w1 + " TO " + w2 + "]").append("</str>");
//
//        q.setFilterQueries("text:[" + w1 + " TO " + w2 + "]");
//        sb.append("<str name=\"sort\">popularity asc</str></lst>");
//        System.out.println(sb.toString());
//        q.setSort("popularity", SolrQuery.ORDER.asc);
//        
//        QueryResponse rsp = client.query(q);
//        if (rsp.getResults().getNumFound() == 0) {
//          System.out.println("Found " + ++zeros + " zero-hit queries so far");
//        }
//
//      }
//    }
//  }
  int numThreads = 10;
  int numDocs = 10_000_000;

  void doIt() throws InterruptedException, IOException, SolrServerException {
    Thread[] threads = new Thread[numThreads];

    for (int idx = 0; idx < threads.length; ++idx) {
      threads[idx] = new Thread(new IndexingThread(idx, numDocs / numThreads));
      threads[idx].start();
    }
    Thread reporter = new Thread(new ReporterThread());
    reporter.start();
    for (int idx = 0; idx < threads.length; ++idx) {
      threads[idx].join();
    }

    shouldStop.getAndSet(true);
    reporter.join();
    for (Thread thread : threads) {
      thread.join();
    }
    try (HttpSolrClient client = new HttpSolrClient.Builder()
        .withBaseSolrUrl(URL)
        .build()) {

//     try ( CloudSolrClient client = new CloudSolrClient.Builder().withZkHost(Main.ZK).build()) {
//      client.setDefaultCollection(COLLECTION);
      client.commit();
    }
  }
}

class ReporterThread implements Runnable {

  long start = System.currentTimeMillis();

  @Override
  public void run() {
    while (Main.shouldStop.get() == false) {
      try {
        Thread.sleep(10_000);
        long intervalSecs = (System.currentTimeMillis() - start) / 1000;
        int grand = Main.grandTotal.get();
        System.out.println(String.format("Indexed %s docs so far in %s seconds, avergage docs/second: %s",
            Main.decFormat.format(grand),
            Main.decFormat.format(intervalSecs),
            Main.decFormat.format(grand / intervalSecs)));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return;
  }

}

class IndexingThread implements Runnable {
  int pct = 1;

  final int id;
  final int numDocs;
  final static Random rand = new Random();

  IndexingThread(int id, int numDocs) throws IOException {
    this.id = id;
    this.numDocs = numDocs;
  }

  static char[] chars = new char[]{'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l',
      'm', 'n', 'o', 'p', 'q', 'r',
      's', 't', 'u', 'v', 'w', 'x',
      'y', 'z'
  };

  static String getSomeRandomJunk() {
    StringBuilder sb = new StringBuilder();
    for (int idx = 0; idx < 100; ++idx) {
      sb.append(" ");
      for (int jdx = 0; jdx < 20; ++jdx) {
        sb.append(chars[rand.nextInt(chars.length)]);
      }
    }
    return sb.toString();
  }

  static int BATCH_SIZE = 1000;
  static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  void addFields(SolrInputDocument doc, String prefix, String val) {
    doc.addField(prefix + "_dv_p_eoe", val);
    doc.addField(prefix + "_ndv_p_eoe", val);
    doc.addField(prefix + "_dv_t_eoe", val);
    doc.addField(prefix + "_ndv_t_eoe", val);
  }

  String getDate(int offset) {
    long loff = offset;
    loff *= 1000;
    loff += 123;
    long d = new Date().getTime();
    String ret = "";
    try {
      ret = sdf.format(new Date(d));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret;
  }

  @Override
  public void run() {

    List<SolrInputDocument> docList = new ArrayList(1001);
//    List<String> groups = new ArrayList<>(Arrays.asList(words.getSomeWords(100_000).split(" ")));
//    List<String> facets = new ArrayList<>(Arrays.asList(words.getSomeWords(100_000).split(" ")));
    try (HttpSolrClient client = new HttpSolrClient.Builder()
        .withBaseSolrUrl(Main.URL)
        .build()) {
//      try (CloudSolrClient client = new CloudSolrClient.Builder().withZkHost(Main.ZK).build()) {
//      client.setDefaultCollection(Main.COLLECTION);
      for (int idx = 0; idx < numDocs; ++idx) {
        //if (rand.nextInt(10) != 4) continue;
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("text_txt", Main.words.getSomeWords(100));
        doc.addField("id", Main.idAtomic.incrementAndGet());
        addFields(doc, "date1M", getDate(rand.nextInt(1_000_000)));
        addFields(doc, "date100M", getDate(rand.nextInt(100_000_000)));
        addFields(doc, "long1M", Integer.toString(rand.nextInt(1_000_000)));
        addFields(doc, "long100M", Integer.toString(rand.nextInt(100_000_000)));
        addFields(doc, "int1M", Integer.toString(rand.nextInt(1_000_000)));
        addFields(doc, "int100M", Integer.toString(rand.nextInt(100_000_000)));
        docList.add(doc);
        if (docList.size() >= BATCH_SIZE) {
          Main.grandTotal.addAndGet(docList.size());
          for (int retry = 0; retry < 3; retry++) {
            try {
              client.add(docList);
//              client.commit();
              break;
            } catch (Exception e) {
              System.out.println("Caught a stupid exception, retrying.");
              e.printStackTrace();
              Thread.sleep(3000);
            }
          }
          docList.clear();
        }
      }
      if (docList.size() > 0) {
        Main.grandTotal.addAndGet(docList.size());
        client.add(docList);
        docList.clear();

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}