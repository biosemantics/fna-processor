import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.inject.name.Names;

import edu.arizona.biosemantics.fnaprocessor.Configuration;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known.KnownCsvReader;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known.KnownVolumeMapper;


public class UpdateCrawlState {

	public static void main(String[] args) throws Exception {
		Map<String, String> volumeUrlNameMap = new LinkedHashMap<String, String>();
		Map<File, String> volumeDirUrlMap = new LinkedHashMap<File, String>();
		Map<String, File> volumeUrlDirMap = new LinkedHashMap<String, File>();
		
		int[] volumes = new int[] {
				6
					//24
				/*2,3,4,5,6,8,9,
				7,
				19,
				22,23,
				24, 25,
				26,27,28*/
			};
			for(int volume : volumes) {
				String volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=10" + String.format("%02d", volume) + "&flora_id=1";
				File volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
				switch(volume) {
				case 2:
					volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
					break;
				case 3:
					volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
					break;
				case 19:
					//volume 19 is for 19-20-21 volumes since they are managed under one and the same url on efloras
					if(volume == 19) {
						volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=1019&flora_id=1";
						volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V19-20-21");
					}
					break;
				case 22:
					volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
				}
				
				volumeUrlNameMap.put(volumeUrl, "v" + volume);
				volumeDirUrlMap.put(volumeDir, volumeUrl);
				volumeUrlDirMap.put(volumeUrl, volumeDir);
		}
			
		File volumesDir = new File(Configuration.fnaTextProcessingDirectory);
			
		
		for(File volumeDir : volumesDir.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.isDirectory() && !file.getName().startsWith(".");
				}
			})) {
			if(volumeDirUrlMap.containsKey(volumeDir)) {
				MapStateProvider mapStateProvider = new KnownVolumeMapper(new KnownCsvReader(volumeUrlDirMap, volumeUrlNameMap));
				MapState mapState = mapStateProvider.getMapState(volumeDir, new MapState(volumeDirUrlMap.get(volumeDir)));
				
				
				SerializedCrawlStateProvider provider = new SerializedCrawlStateProvider(new File("crawlState"), volumeUrlNameMap);
				CrawlState crawlState = provider.getCrawlState(volumeDirUrlMap.get(volumeDir));
				
				int i = 1;
				for(File file : volumeDir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return file.isFile() && file.getName().endsWith(".xml");
					}
				})) {
					System.out.println(i++);
					String url = mapState.getUrl(file);
					if(!crawlState.containsUrlDocumentMapping(url)) {
						Document document = Jsoup.connect(url).get();
						crawlState.putUrlDocumentMapping(url, document);
						System.out.println("Getting document missing: " + url);
					} else {
						System.out.println("Already have doc...");
					}
				}
				
				SerializedCrawlStateStorer storer = new SerializedCrawlStateStorer(new File("crawlState2"), volumeUrlNameMap);
				storer.store(crawlState);
			}
		}
			
			
	}
	
}
