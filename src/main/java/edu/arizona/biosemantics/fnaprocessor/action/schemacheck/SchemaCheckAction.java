package edu.arizona.biosemantics.fnaprocessor.action.schemacheck;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;

import org.apache.log4j.Logger;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

public class SchemaCheckAction implements VolumeAction {

	private static final Logger logger = Logger.getLogger(SchemaCheckAction.class);
	
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Check schema for volume " + volumeDir);
		
		XMLValidator xmlValidator = new XMLValidator(new File("src/main/resources/edu/arizona/biosemantics/fnaprocessor/semanticMarkupInput.xsd"));
		
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			String content = new String(Files.readAllBytes(file.toPath()));
			if(!xmlValidator.validate(content)) {
				logger.error("Invalid volume file: " + file.getName());
				logger.error(xmlValidator.getInvalidMessage());
			}
		}
	}

}
