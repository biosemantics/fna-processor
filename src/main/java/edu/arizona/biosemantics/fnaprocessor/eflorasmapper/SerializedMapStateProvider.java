package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateProvider;

public class SerializedMapStateProvider implements MapStateProvider {

	private static final Logger logger = Logger.getLogger(SerializedCrawlStateProvider.class);
	
	private File mapStateDir;
	private Map<File, String> volumeDirNameMap;
	private Map<String, String> volumeUrlNameMap;

	public SerializedMapStateProvider(File mapStateDir, Map<File, String> volumeDirNameMap, 
			Map<String, String> volumeUrlNameMap) {
		this.mapStateDir = mapStateDir;
		this.volumeDirNameMap = volumeDirNameMap;
		this.volumeUrlNameMap = volumeUrlNameMap;
	}
	
	@Override
	public MapState getMapState(File volumeDir) throws Exception {
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				new File(mapStateDir, volumeUrlNameMap.get(volumeDirNameMap.get(volumeDir)))))) {
            Object object = in.readObject();
            return (MapState)object;
        }
	}

}
