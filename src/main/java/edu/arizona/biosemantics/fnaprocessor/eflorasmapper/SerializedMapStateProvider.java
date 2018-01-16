package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateProvider;

public class SerializedMapStateProvider implements MapStateProvider {

	private static final Logger logger = Logger.getLogger(SerializedCrawlStateProvider.class);
	
	private File mapStateDir;
	private Map<File, String> volumeDirUrlMap;
	private Map<String, String> volumeUrlNameMap;

	@Inject
	public SerializedMapStateProvider(
			@Named("serializedMapStateDir") File mapStateDir, 
			@Named("volumeDirUrlMap") Map<File, String> volumeDirUrlMap, 
			@Named("volumeUrlNameMap") Map<String, String> volumeUrlNameMap) {
		this.mapStateDir = mapStateDir;
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.volumeUrlNameMap = volumeUrlNameMap;
	}
	
	@Override
	public MapState getMapState(File volumeDir, MapState mapState) throws Exception {
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				new File(mapStateDir, volumeUrlNameMap.get(volumeDirUrlMap.get(volumeDir)))))) {
            Object object = in.readObject();
            return (MapState)object;
        }
	}

}
