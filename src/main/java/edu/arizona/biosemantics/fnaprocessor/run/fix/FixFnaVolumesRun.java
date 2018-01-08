package edu.arizona.biosemantics.fnaprocessor.run.fix;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.run.Run;

public class FixFnaVolumesRun implements Run {

	private List<VolumeAction> actions;
	private File volumesDir;

	public FixFnaVolumesRun(
			@Named("volumesDirectory") File volumesDir,
			List<VolumeAction> actions) {
		this.volumesDir = volumesDir;
		this.actions = actions;
	}
	
	@Override
	public void run() throws Exception {
		for(VolumeAction action : actions) {
			for(File volumeDir : volumesDir.listFiles(new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory();
					}
				})) {
				action.run(volumeDir);
			}
		}
	}

}
