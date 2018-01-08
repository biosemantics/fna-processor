package edu.arizona.biosemantics.fnaprocessor;

import java.util.Properties;

public class Configuration {
	
	private static Properties properties;
	public static String fnaTextProcessingDirectory;
	
	static {		
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			properties = new Properties(); 
			properties.load(loader.getResourceAsStream("edu/arizona/biosemantics/fnafix/config.properties"));
			
			fnaTextProcessingDirectory = properties.getProperty("fnaTextProcessingDirectory");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
