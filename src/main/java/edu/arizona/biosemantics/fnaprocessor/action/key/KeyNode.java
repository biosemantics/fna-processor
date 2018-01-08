package edu.arizona.biosemantics.fnaprocessor.action.key;

import java.util.List;

public class KeyNode {

	private List<TaxonConcept> lowerTaxonNames;
	private TaxonConcept taxonName;
	
	
	
	public KeyNode(TaxonConcept taxonName, List<TaxonConcept> lowerTaxonNames) {
		super();
		this.lowerTaxonNames = lowerTaxonNames;
		this.taxonName = taxonName;
	}
	public List<TaxonConcept> getLowerTaxonConcepts() {
		return lowerTaxonNames;
	}
	public TaxonConcept getTaxonName() {
		return taxonName;
	}
	
	public String toString() {
		return this.taxonName.toString();
	}
	
}
