package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;

/**
 * Gets a MapState by using a CSV files content
 */
public class KnownVolumeMapper implements MapStateProvider {

	private static final Logger logger = Logger.getLogger(KnownVolumeMapper.class);
	private KnownCsvReader knownCsvReader;

	/**
	 * @param knownCsvReader: To read a CSV file
	 */
	@Inject
	public KnownVolumeMapper(
			KnownCsvReader knownCsvReader) {
		this.knownCsvReader = knownCsvReader;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MapState getMapState(File volumeDir, MapState mapState)
			throws Exception {
		Map<String, File> knownMapping = this.knownCsvReader.read(mapState.getVolumeUrl());
		for(String url : knownMapping.keySet()) {
			mapState.putFileUrlMap(knownMapping.get(url), url, this.getClass());
		}
		logger.trace("Done mapping known urls");
		return mapState;
	}

}
