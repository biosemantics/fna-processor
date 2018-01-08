package edu.arizona.biosemantics.fnaprocessor.action.key;

public class TaxonConcept {

	private String author;
	private String name;
	
	public TaxonConcept(String name, String author) {
		this.name = name;
		this.author = author;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getName() {
		return name;
	}	
	
	public String toString() {
		return this.name + "_" + this.author;
	}
	
}
