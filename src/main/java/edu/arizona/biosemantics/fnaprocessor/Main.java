package edu.arizona.biosemantics.fnaprocessor;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import edu.arizona.biosemantics.fnaprocessor.action.crawler.Crawler;
import edu.arizona.biosemantics.fnaprocessor.action.duplicate.FindDuplicateAction;
import edu.arizona.biosemantics.fnaprocessor.action.taxonname.TaxonNameValidationAction;

public class Main {

	public static void main(String[] args) throws Exception {
		List<VolumeAction> actions = new ArrayList<VolumeAction>();
		actions.add(new FindDuplicateAction());
		actions.add(new TaxonNameValidationAction());
		File file = new File(Configuration.fnaTextProcessingDirectory);
		for(VolumeAction action : actions) {
			for(File volumeDir : file.listFiles(new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory();
					}
				})) {
				action.run(volumeDir);
			}
		}
	}
}
