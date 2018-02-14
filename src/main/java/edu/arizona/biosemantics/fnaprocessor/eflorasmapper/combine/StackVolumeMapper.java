package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.combine;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateReporter;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known.KnownVolumeMapper;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.name.NameBasedVolumeMapper;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.number.NumberBasedVolumeMapper;

/**
 * A VolumeMapper that uses a stack of other volumeMappers to one by one map
 * files to documents
 */
public class StackVolumeMapper implements MapStateProvider {

	private static final Logger logger = Logger.getLogger(StackVolumeMapper.class);
	private ArrayList<MapStateProvider> mapStateProviderStack;
	private MapStateReporter mapStateReporter;

	/**
	 * @param knownVolumeMapper: The knownVolumeMapper to use in the stack
	 * @param numberBasedMapper: The numberBasedMapper to use in the stack
	 * @param nameBasedMapper: The nameBasedMapper to use in the stack
	 * @param mapStateReporter: The mapStateReporter to use to report on the resulting mapState
	 */
	@Inject
	public StackVolumeMapper(KnownVolumeMapper knownVolumeMapper,
			NumberBasedVolumeMapper numberBasedMapper,
			NameBasedVolumeMapper nameBasedMapper,
			MapStateReporter mapStateReporter) {
		this.mapStateReporter = mapStateReporter;
		this.mapStateProviderStack = new ArrayList<MapStateProvider>();
		this.mapStateProviderStack.add(knownVolumeMapper);
		this.mapStateProviderStack.add(numberBasedMapper);
		this.mapStateProviderStack.add(nameBasedMapper);
		this.mapStateProviderStack.add(numberBasedMapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MapState getMapState(File volumeDir, MapState mapState) throws Exception {
		for(MapStateProvider mapStateProvider : mapStateProviderStack) {
			logger.info("Map urls to file using " + mapStateProvider.getClass().getCanonicalName());
			mapState = mapStateProvider.getMapState(volumeDir, mapState);
			mapStateReporter.report(mapState);
		}
		return mapState;
	}

}
