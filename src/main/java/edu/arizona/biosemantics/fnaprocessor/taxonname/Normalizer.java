package edu.arizona.biosemantics.fnaprocessor.taxonname;

public class Normalizer {

	public static String normalize(String value) {
		if(value == null)
			System.out.println();
		value = value.replaceAll("ë", "");
		return value.trim().replaceAll("[^a-zA-Z_0-9.<>\\s]", "").replaceAll("\\s+", " ").toLowerCase();
	}
	
}
