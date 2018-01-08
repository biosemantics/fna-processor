package edu.arizona.biosemantics.fnaprocessor.taxonname;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CollectiveNameExtractor implements TaxonNameExtractor {

	private Set<TaxonNameExtractor> childExtractors;
	private Map<File, Set<String>> nameExtractionCache = new HashMap<File, Set<String>>();

	public CollectiveNameExtractor(Set<TaxonNameExtractor> childExtractors) {
		this.childExtractors = childExtractors;
	}
	
	@Override
	public Set<String> extract(File file) throws Exception {
		if(nameExtractionCache.containsKey(file))
			return nameExtractionCache.get(file);
		
		Set<String> candidates = new HashSet<String>();
		for(TaxonNameExtractor extractor : this.childExtractors) {
			candidates.addAll(extractor.extract(file));
		}
		
		nameExtractionCache.put(file, candidates);
		return candidates;
	}
	
}
