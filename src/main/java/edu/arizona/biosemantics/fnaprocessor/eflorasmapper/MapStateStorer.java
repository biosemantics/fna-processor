package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

/**
 * Strores a MapState
 */
public interface MapStateStorer {

	/**
	 * Stores a map state
	 * @param mapState: The state to store
	 * @throws Exception if there was a problem storing the state
	 */
	public void store(MapState mapState) throws Exception;

}
