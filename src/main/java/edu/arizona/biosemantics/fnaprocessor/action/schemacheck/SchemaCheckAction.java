package edu.arizona.biosemantics.fnaprocessor.action.schemacheck;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.apache.log4j.Logger;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

/**
 * SchemaCheckAction validated the CharaParser input schema against all the files of the volumes directory.
 * Invalid files are reported in a file stored in {volumeDir}/invalid-against-input-schema.txt
 */
public class SchemaCheckAction implements VolumeAction {

	private static final Logger logger = Logger.getLogger(SchemaCheckAction.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Check schema for volume " + volumeDir);
		StringBuilder sb = new StringBuilder();

		XMLValidator xmlValidator = new XMLValidator(new File("src/main/resources/edu/arizona/biosemantics/fnaprocessor/semanticMarkupInput.xsd"));

		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			String content = new String(Files.readAllBytes(file.toPath()));
			if(!xmlValidator.validate(content)) {
				sb.append("Invalid volume file: " + file.getName() + "\n");
				sb.append(xmlValidator.getInvalidMessage() + "\n");
				logger.error("Invalid volume file: " + file.getName());
				logger.error(xmlValidator.getInvalidMessage());

			}
		}

		String report = sb.toString().trim();
		if(!report.isEmpty())
			try(PrintWriter out = new PrintWriter(
					new File(volumeDir, "invalid-against-input-schema.txt"))) {
				out.println(report);
			}
	}
}
