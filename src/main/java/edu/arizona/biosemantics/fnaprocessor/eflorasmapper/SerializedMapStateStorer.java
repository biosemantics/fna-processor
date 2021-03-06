package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.SerializedCrawlStateStorer;

/**
 * Stores a MapState in java serialized format
 */
public class SerializedMapStateStorer implements MapStateStorer {

	private static final Logger logger = Logger.getLogger(SerializedCrawlStateStorer.class);
	private File targetDir;
	private Map<String, String> volumeUrlNameMap;

	/**
	 * @param targetDir: The directory where to store the MapState
	 * @param volumeUrlNameMap: To find the volume name for a given volume url
	 */
	@Inject
	public SerializedMapStateStorer(@Named("serializedMapStateDir")File targetDir,
			@Named("volumeUrlNameMap")Map<String, String> volumeUrlNameMap) {
		this.targetDir = targetDir;
		this.volumeUrlNameMap = volumeUrlNameMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(MapState mapState) throws Exception {
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				new File(targetDir, this.volumeUrlNameMap.get(mapState.getVolumeUrl()))))) {
			out.writeObject(mapState);
			logger.info("Completed writing map state for " +
					this.volumeUrlNameMap.get(mapState.getVolumeUrl()) + " to " + this.targetDir);
		}
	}

}
