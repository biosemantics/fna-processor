package edu.arizona.biosemantics.fnaprocessor.action.taxonname;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

public class TaxonNameValidationAction implements VolumeAction {

	private static final Logger logger = Logger.getLogger(TaxonNameValidationAction.class);
	
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Validating taxon names for " + volumeDir.getAbsolutePath());
		StringBuilder report = new StringBuilder();
		
		TaxonNameValidator validator = new TaxonNameValidator();	
		boolean valid = validator.validate(volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		}));
		if(!valid) {
			try(PrintWriter out = new PrintWriter(
					new File(volumeDir, "invalid-taxon-names.txt"))) {
			    out.println(validator.getInvalidMessage());
			}
			logger.error(validator.getInvalidMessage());
		}

	}

}
