package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.arizona.biosemantics.fnaprocessor.Configuration;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;

public class AdaptChangedStructure {

	public static void main(String[] args) throws Exception {
		int[] volumes = new int[] {
				3 };
		//19 };//,3,4,5,6,7,8,9,19,22,23,24,25,26,27,28 };
		Map<File, String> volumeDirUrlMap = new LinkedHashMap<File, String>();
		Map<Integer, File> volumeDirMap = new LinkedHashMap<Integer, File>();
		Map<String, String> volumeUrlNameMap = new LinkedHashMap<String, String>();
		Map<String, File> volumeUrlDirMap = new LinkedHashMap<String, File>();

		for(int volume : volumes) {
			String volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=10" + String.format("%02d", volume) + "&flora_id=1";
			File volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
			switch(volume) {
			case 2:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
				break;
			case 3:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
				break;
			case 19:
				//volume 19 is for 19-20-21 volumes since they are managed under one and the same url on efloras
				if(volume == 19) {
					volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=1019&flora_id=1";
					volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V19-20-21");
				}
				break;
			case 22:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
			}

			volumeDirMap.put(volume, volumeDir);
			volumeDirUrlMap.put(volumeDir, volumeUrl);
			volumeUrlNameMap.put(volumeUrl, "v" + volume);
			volumeUrlDirMap.put(volumeUrl, volumeDir);
		}

		for(int volume : volumes) {
			File volumeDir = volumeDirMap.get(volume);
			String volumeUrl = volumeDirUrlMap.get(volumeDir);

			KnownCsvReader reader = new KnownCsvReader(volumeUrlDirMap, volumeUrlNameMap);
			KnownVolumeMapper mapper = new KnownVolumeMapper(reader);
			MapState mapState = mapper.getMapState(volumeDir, new MapState(volumeDirUrlMap.get(volumeDir)));

			MapState newMapState = new MapState(volumeUrl);

			String prefix = "V" + volume + "_";
			for(File file : mapState.getMappedFiles()) {
				if(!file.getName().startsWith(prefix)) {
					File newFile = new File(file.getParent(), prefix + file.getName());
					newMapState.putFileUrlMap(newFile, mapState.getUrl(file), mapState.getMapper(file));
				} else {
					if(file.getName().startsWith("v19")) {
						File newFile = new File(file.getParent(), file.getName().replace("v19-", "V19-"));
						newMapState.putFileUrlMap(newFile, mapState.getUrl(file), mapState.getMapper(file));
					}
					if(file.getName().startsWith("v20")) {
						File newFile = new File(file.getParent(), file.getName().replace("v20-", "V20-"));
						newMapState.putFileUrlMap(newFile, mapState.getUrl(file), mapState.getMapper(file));
					}
					if(file.getName().startsWith("v21")) {
						File newFile = new File(file.getParent(), file.getName().replace("v21-", "V21-"));
						newMapState.putFileUrlMap(newFile, mapState.getUrl(file), mapState.getMapper(file));
					}
				}
			}


			SerializedCrawlStateProvider provider = new SerializedCrawlStateProvider(new File("crawlState"), volumeUrlNameMap);
			KnownCsvWriter writer = new KnownCsvWriter(provider, volumeUrlDirMap, volumeUrlNameMap);
			writer.write(newMapState);
		}
	}
}
