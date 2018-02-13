package edu.arizona.biosemantics.fnaprocessor.action;

import java.io.File;

/**
 * A VolumeAction is run a volumes directory to carry out some action on its files
 */
public interface VolumeAction {

	/**
	 * Runs the action
	 * @param volumeDir: The volumeDir to run the action on
	 * @throws Exception if there was a problem running the action
	 */
	public abstract void run(File volumeDir) throws Exception;

}
