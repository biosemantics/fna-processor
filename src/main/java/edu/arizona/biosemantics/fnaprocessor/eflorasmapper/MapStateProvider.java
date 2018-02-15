package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;

/**
 * Can provide a MapState (see {@link MapState})
 */
public interface MapStateProvider {

	/**
	 * @param volumeDir for which to return the MapState
	 * @param mapState: The MapState to use as initialization for the mapping to provide
	 * @return the MapState provided
	 * @throws Exception if there was a problem retrieving the MapState
	 */
	public MapState getMapState(File volumeDir, MapState mapState) throws Exception;


}
