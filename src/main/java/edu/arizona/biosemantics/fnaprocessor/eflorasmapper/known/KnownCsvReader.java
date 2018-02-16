package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Reads a CSV format capturing file to eflora url mapping into a Map<String, File>
 */
public class KnownCsvReader {

	private Map<String, File> volumeUrlDirMap;
	private Map<String, String> volumeUrlNameMap;

	/**
	 *
	 * @param volumeUrlDirMap: To map from volume url to dir
	 * @param volumeUrlNameMap: To map from volume url to name
	 */
	@Inject
	public KnownCsvReader(@Named("volumeUrlDirMap")Map<String, File> volumeUrlDirMap,
			@Named("volumeUrlNameMap")Map<String, String> volumeUrlNameMap) {
		this.volumeUrlDirMap = volumeUrlDirMap;
		this.volumeUrlNameMap= volumeUrlNameMap;
	}

	/**
	 * Reads a CSV format capturing file to eflora url mapping into a Map<String, File>
	 * @param volumeUrl: The volume url for which to read the mappings
	 * @return the map of url to file map
	 * @throws FileNotFoundException if the file could not be found
	 * @throws IOException if the file could not be read
	 */
	public Map<String, File> read(String volumeUrl) throws FileNotFoundException, IOException {
		Map<String, File> result = new HashMap<String, File>();
		File known = new File("known-" + volumeUrlNameMap.get(volumeUrl) + ".csv");
		if(known.exists() && known.isFile()) {
			try(CSVReader reader = new CSVReader(new FileReader(new File("known-" + volumeUrlNameMap.get(volumeUrl) + ".csv")))) {
				List<String[]> lines = reader.readAll();
				for(String[] line : lines) {
					if(line[0].trim().isEmpty() || line[1].trim().isEmpty())
						continue;

					if(line[0].contains(")")) {
						line[0] = line[0].split("\\)")[1].trim();
					}
					if(line[1].contains(")")) {
						line[1] = line[1].split("\\)")[1].trim();
					}
					result.put(line[1], new File(volumeUrlDirMap.get(volumeUrl), line[0]));
				}
			}
		}
		return result;
	}

}
