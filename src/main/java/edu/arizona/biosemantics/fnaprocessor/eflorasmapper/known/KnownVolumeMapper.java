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
	private KnownCsvReader knownCsvReader;

	@Inject
	public KnownVolumeMapper(
			KnownCsvReader knownCsvReader) {
		this.knownCsvReader = knownCsvReader;
	}
	
	@Override
	public MapState getMapState(File volumeDir, MapState mapState)
			throws Exception {
		Map<String, File> knownMapping = this.knownCsvReader.read(mapState.getVolumeUrl());
		for(String url : this.knownCsvReader.read(mapState.getVolumeUrl()).keySet()) {
			mapState.putFileUrlMap(knownMapping.get(url), url);
		}
		logger.trace("Done mapping known urls");
		return mapState;
	}

}
