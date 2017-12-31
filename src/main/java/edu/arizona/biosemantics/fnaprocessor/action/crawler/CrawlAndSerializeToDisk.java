package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.JDOMException;
import org.jsoup.Jsoup;

public class CrawlAndSerializeToDisk {

	public static void main(String[] args) throws IOException, JDOMException, ClassNotFoundException {
		
		int[] volumes = new int[] {
			/*2,3,4,5,6,7,*/8,9,19,22,23,24,25,26,27,28
			
			/*,20,21*/
		};
		for(int volume : volumes) {
			String baseUrl = "http://www.efloras.org/";
			String volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=10" + String.format("%02d", volume) + "&flora_id=1";
			File volumeDir = new File("C:\\Users\\updates\\git\\FNATextProcessing\\V" + volume);
			if(volume == 19) {
				volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=1019&flora_id=1";
				volumeDir = new File("C:\\Users\\updates\\git\\FNATextProcessing\\V19-20-21");
			}
			Map<String, File> knownUrlToFileMappings = new HashMap<String, File>();
			Crawler crawler = new Crawler(new HrefResolver(baseUrl), volumeUrl, volumeDir, knownUrlToFileMappings);
			crawler.crawl();
		
	        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
	        		"crawlState-v" + volume + ".ser"))) {
	            out.writeObject(crawler.getCrawlState());
	        }
		}
		

		/*int volume = 99;
		CrawlState crawlState = new CrawlState();
		crawlState.putFileToDocumentMapping(new File(""), Jsoup.connect("http://google.com").get());
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
        		"crawlState-v" + volume + ".ser"))) {
            out.writeObject(crawlState);
        }
		
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(
        		"crawlState-v" + volume + ".ser"))) {
            Object object = in.readObject();
            System.out.println(object);
        }*/
	}
}
