package edu.arizona.biosemantics.fnaprocessor.taxonname;

import java.io.File;
import java.util.Set;


public interface TaxonNameExtractor {
	
	public Set<String> extract(File file) throws Exception;
}
