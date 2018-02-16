package edu.arizona.biosemantics.fnaprocessor.taxonname;

import java.io.File;
import java.util.Set;

/**
 * Extracts a set of candidate taxon names as they may be found elsewhere, e.g. eflora
 */
public interface TaxonNameExtractor {

	/**
	 * @param file: The file to extract the candiate taxon names from
	 * @return a set of taxon names
	 * @throws Exception if there was a problem extracting the taxon names from file
	 */
	public Set<String> extract(File file) throws Exception;
}
