package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;

public class KnownVolumeMapper implements MapStateProvider {
	
	private static final Logger logger = Logger.getLogger(KnownVolumeMapper.class);
	private Map<String, File> knownUrlFileMap;

	@Inject
	public KnownVolumeMapper(
			@Named("knownFileUrlMap") Map<String, File> knownFileUrlMap) {
		this.knownUrlFileMap = knownFileUrlMap;
	}
	
	@Override
	public MapState getMapState(File volumeDir, MapState mapState)
			throws Exception {
		for(String url : knownUrlFileMap.keySet()) {
			if(!mapState.hasFile(url))
				mapState.putFileUrlMap(knownUrlFileMap.get(url), url);
		}
		logger.trace("Done mapping known urls");
		return mapState;
	}

}
