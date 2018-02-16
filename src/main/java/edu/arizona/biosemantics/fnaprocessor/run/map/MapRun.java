package edu.arizona.biosemantics.fnaprocessor.run.map;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.DefaultMapStateReporter;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known.KnownCsvReader;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known.KnownCsvWriter;
import edu.arizona.biosemantics.fnaprocessor.run.Run;

/**
 * Run to map fna volumes to eflora documents
 */
public class MapRun implements Run {

	private static Logger logger = Logger.getLogger(MapRun.class);
	private MapStateProvider mapStateProvider;
	private Map<File, String> volumeDirUrlMap;
	private MapStateStorer mapStateStorer;
	private DefaultMapStateReporter reporter;
	private KnownCsvWriter knownCsvWriter;
	private KnownCsvReader knownCsvReader;

	/**
	 * @param mapStateProvider: The mapStateProvider to use to create the mapping
	 * @param volumeDirUrlMap: to get the volume url from the volume dir
	 * @param mapStateStorer: to store the created mapState
	 * @param reporter: to report the created mapState
	 * @param knownCsvWriter: to store the mapState to a CSV file
	 * @param knownCsvReader: to initialize the mapState from a preexisting CSV file
	 */
	@Inject
	public MapRun(MapStateProvider mapStateProvider,
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap,
			MapStateStorer mapStateStorer,
			DefaultMapStateReporter reporter ,
			KnownCsvWriter knownCsvWriter,
			KnownCsvReader knownCsvReader) {
		this.mapStateProvider = mapStateProvider;
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.mapStateStorer = mapStateStorer;
		this.reporter = reporter;
		this.knownCsvWriter = knownCsvWriter;
		this.knownCsvReader = knownCsvReader;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() throws Exception {
		for(File volumeDir : volumeDirUrlMap.keySet()) {
			MapState mapState = mapStateProvider.getMapState(volumeDir, new MapState(volumeDirUrlMap.get(volumeDir)));
			reporter.report(mapState);
			this.mapStateStorer.store(mapState);
			this.knownCsvWriter.write(mapState);
		}
	}
}
