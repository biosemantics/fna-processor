package edu.arizona.biosemantics.fnaprocessor;

import java.util.Properties;

/**
 * Configuration reads the static configuration information (e.g. the directory location
 * with the fna volumes) from the properties file at
 * src/main/resources/edu/arizona/biosemantics/fnaprocessor/config.properties to easily reference
 * them in code.
 *
 */
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
