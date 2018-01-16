package edu.arizona.biosemantics.fnaprocessor.run.map;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateReporter;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateReporter2;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateStorer;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.VolumeMapper2;
import edu.arizona.biosemantics.fnaprocessor.run.Run;

public class MapRun implements Run {
	
	private static Logger logger = Logger.getLogger(MapRun.class);
	private VolumeMapper2 volumeMapper;
	private Map<File, String> volumeDirUrlMap;
	private MapStateStorer mapStateStorer;
	private MapStateReporter2 reporter;
	
	@Inject
	public MapRun(VolumeMapper2 volumeMapper,
			@Named("volumeDirUrlMap")Map<File, String> volumeDirUrlMap,
			MapStateStorer mapStateStorer, 
			MapStateReporter2 reporter) {
		this.volumeMapper = volumeMapper;
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.mapStateStorer = mapStateStorer;
		this.reporter = reporter;
	}
	
	@Override
	public void run() throws Exception {
		for(File volumeDir : volumeDirUrlMap.keySet()) {
			MapState mapState = volumeMapper.map(volumeDir);
			reporter.report(mapState);
			this.mapStateStorer.store(mapState);
		}
	}
}
