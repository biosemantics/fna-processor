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

public class MapRun implements Run {
	
	private static Logger logger = Logger.getLogger(MapRun.class);
	private MapStateProvider mapStateProvider;
	private Map<File, String> volumeDirUrlMap;
	private MapStateStorer mapStateStorer;
	private DefaultMapStateReporter reporter;
	private KnownCsvWriter knownCsvWriter;
	private KnownCsvReader knownCsvReader;
	
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
	
	@Override
	public void run() throws Exception {
		for(File volumeDir : volumeDirUrlMap.keySet()) {
			MapState mapState = mapStateProvider.getMapState(volumeDir, new MapState(volumeDirUrlMap.get(volumeDir)));
			Map<String, File> knownMapping = this.knownCsvReader.read(mapState.getVolumeUrl());
			for(String url : this.knownCsvReader.read(mapState.getVolumeUrl()).keySet()) {
				mapState.putFileUrlMap(knownMapping.get(url), url);
			}
			reporter.report(mapState);
			this.mapStateStorer.store(mapState);
			this.knownCsvWriter.write(mapState);
		}
	}
}
