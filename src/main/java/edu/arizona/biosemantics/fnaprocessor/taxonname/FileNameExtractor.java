package edu.arizona.biosemantics.fnaprocessor.taxonname;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FileNameExtractor implements TaxonNameExtractor {

	@Override
	public Set<String> extract(File file) throws Exception {
		Set<String> result = new HashSet<String>();
		result.add(Normalizer.normalize(file.getName().replace(".xml", "")));
		return result;
	}

}
