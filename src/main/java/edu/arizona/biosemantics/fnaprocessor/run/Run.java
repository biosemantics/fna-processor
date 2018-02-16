package edu.arizona.biosemantics.fnaprocessor.run;

/**
 * A run of a phase of the fna-processor
 */
public interface Run {

	/**
	 * Runs the phase
	 * @throws Exception if there was a problem running the phase
	 */
	public void run() throws Exception;

}
