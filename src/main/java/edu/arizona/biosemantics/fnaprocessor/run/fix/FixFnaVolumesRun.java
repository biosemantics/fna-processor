package edu.arizona.biosemantics.fnaprocessor.run.fix;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.action.distributionmap.DistributionMapAction;
import edu.arizona.biosemantics.fnaprocessor.action.duplicate.FindDuplicateAction;
import edu.arizona.biosemantics.fnaprocessor.action.duplicateelement.DuplicateElementAction;
import edu.arizona.biosemantics.fnaprocessor.action.key.KeyAction;
import edu.arizona.biosemantics.fnaprocessor.action.parenthesis.ParenthesisAction;
import edu.arizona.biosemantics.fnaprocessor.action.printlocation.PrintLocationAction;
import edu.arizona.biosemantics.fnaprocessor.action.schemacheck.SchemaCheckAction;
import edu.arizona.biosemantics.fnaprocessor.action.taxonname.TaxonNameValidationAction;
import edu.arizona.biosemantics.fnaprocessor.run.Run;

public class FixFnaVolumesRun implements Run {

	private List<VolumeAction> actions;
	private File volumesDir;
	private Map<File, String> volumeDirUrlMap;

	@Inject
	public FixFnaVolumesRun(
			@Named("volumesDir") File volumesDir,
			@Named("volumeDirUrlMap") Map<File, String> volumeDirUrlMap,
			FindDuplicateAction findDuplicateAction,
			TaxonNameValidationAction taxonNameValidationAction,
			KeyAction keyAction,
			DistributionMapAction distributionMapAction,
			PrintLocationAction printLocationAction,
			ParenthesisAction parenthesisAction,
			//ConvertOldSchemaAction schemaAction,
			SchemaCheckAction schemaCheckAction,
			DuplicateElementAction duplicateElementAction
			) {
		this.volumesDir = volumesDir;
		VolumeAction[] a = {
				findDuplicateAction,
				taxonNameValidationAction,
				printLocationAction,
				distributionMapAction,
				keyAction,
				parenthesisAction,
				//schemaAction,*/
				duplicateElementAction,
				schemaCheckAction
		};
		this.volumeDirUrlMap = volumeDirUrlMap;
		this.actions = new ArrayList<VolumeAction>(Arrays.asList(a));
	}

	@Override
	public void run() throws Exception {
		for(VolumeAction action : actions) {
			for(File volumeDir : volumesDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory() && !file.getName().startsWith(".");
				}
			})) {
				if(volumeDirUrlMap.containsKey(volumeDir)) {
					action.run(volumeDir);
				}
			}
		}
	}

}
