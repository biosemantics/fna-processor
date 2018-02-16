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

/**
 * Run to fix fna volumes from various problems for which there exist fixing actions
 */
public class FixFnaVolumesRun implements Run {

	private List<VolumeAction> actions;
	private File volumesDir;
	private Map<File, String> volumeDirUrlMap;

	/**
	 * @param volumesDir: The volume dir to fix
	 * @param volumeDirUrlMap: to map from volume dir to name
	 * @param findDuplicateAction: To find duplicate files
	 * @param taxonNameValidationAction: To find duplicate taxon names
	 * @param keyAction: To add keys from eflora
	 * @param distributionMapAction: To create distribution maps from eflora
	 * @param printLocationAction: To add the print location in publication to the files
	 * @param parenthesisAction: To check for unclosed parenthesis
	 * @param schemaCheckAction: To check for XML file validity against schema
	 * @param duplicateElementAction: To check for duplicate elements within a file andparent  element
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
