package edu.arizona.biosemantics.fnaprocessor.action.taxonname;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

public class TaxonNameValidationAction implements VolumeAction {

	private static final Logger logger = Logger.getLogger(TaxonNameValidationAction.class);
	
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("--------------------------------------");
		logger.info("Validating taxon names for " + volumeDir.getAbsolutePath());
		
		TaxonNameValidator validator = new TaxonNameValidator();	
		boolean valid = validator.validate(volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		}));
		if(!valid)
			logger.error(validator.getInvalidMessage());

	}

}
