package edu.arizona.biosemantics.fnaprocessor.run.fix;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.arizona.biosemantics.fnaprocessor.run.Run;
import edu.arizona.biosemantics.fnaprocessor.run.fix.Config;

public class Main {

	public static void main(String[] args) throws Exception {
		Config config = new Config();
		Injector injector = Guice.createInjector(config);
		FixFnaVolumesRun run = injector.getInstance(FixFnaVolumesRun.class);
		run.run();
	}
}
