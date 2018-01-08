package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;

public interface MapStateProvider {

	public MapState getMapState(File volumeDir) throws Exception;

}
