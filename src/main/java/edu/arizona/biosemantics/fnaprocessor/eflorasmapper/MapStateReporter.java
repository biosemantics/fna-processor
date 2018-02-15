package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

/**
 * Reports the map state to the log
 */
public interface MapStateReporter {

	/**
	 * Reports the map state to the log
	 * @param mapState: The state to report
	 * @throws Exception if there was a problem reporting the state
	 */
	public void report(MapState mapState) throws Exception;
}
