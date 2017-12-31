package edu.arizona.biosemantics.fnaprocessor;

import java.io.File;

public interface VolumeAction {

	public abstract void run(File volumeDir) throws Exception;

}
