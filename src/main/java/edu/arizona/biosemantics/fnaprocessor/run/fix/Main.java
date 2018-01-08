package edu.arizona.biosemantics.fnaprocessor.run.fix;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {

	public static void main(String[] args) throws Exception {
		Config config = new Config();
		Injector injector = Guice.createInjector(config);
		FixFnaVolumesRun run = injector.getInstance(FixFnaVolumesRun.class);
		run.run();
	}
}
