package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import au.com.bytecode.opencsv.CSVReader;

public class KnownCsvReader {

	private Map<String, File> volumeUrlDirMap;
	private Map<String, String> volumeUrlNameMap;

	@Inject
	public KnownCsvReader(@Named("volumeUrlDirMap")Map<String, File> volumeUrlDirMap,
			@Named("volumeUrlNameMap")Map<String, String> volumeUrlNameMap) {
		this.volumeUrlDirMap = volumeUrlDirMap;
		this.volumeUrlNameMap= volumeUrlNameMap;
	}
	
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
