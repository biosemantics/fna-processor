package edu.arizona.biosemantics.fnaprocessor;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

public class Configuration {
	
	private static Properties properties;
	public static String fnaTextProcessingDirectory;
	
	static {		
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			properties = new Properties(); 
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/fnaprocessor/config.properties"));
			
			fnaTextProcessingDirectory = properties.getProperty("fnaTextProcessingDirectory");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
