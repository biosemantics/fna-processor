package edu.arizona.biosemantics.fnaprocessor.run.crawl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateStorer;

public class Test {

	public static void main(String[] args) throws Exception {
		/*CrawlState state = new CrawlState("");
		String url = "http://test";
		Document doc = Jsoup.parse("<html></html>");
		state.putUrlDocumentMapping(url, doc);
		state.putReverseUrlDocumentMapping(doc, url);
		
		Map<String, String> volumeUrlNameMap = new HashMap<String, String>();
		volumeUrlNameMap.put("", "name");
		CrawlStateStorer storer = new SerializedCrawlStateStorer(new File("mystate"), volumeUrlNameMap);
		storer.store(state);
		
		CrawlStateProvider provider = new SerializedCrawlStateProvider(new File("mystate"), volumeUrlNameMap);
		CrawlState crawlState = provider.getCrawlState("");
		
		for(Document document : crawlState.getDocuments()) {
			String url2 = crawlState.getUrl(document);
			System.out.println(url2);
		}
		*/
	}
	
}


