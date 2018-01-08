package edu.arizona.biosemantics.fnaprocessor.run.schema;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.arizona.biosemantics.fnaprocessor.run.Run;
import edu.arizona.biosemantics.fnaprocessor.run.fix.Config;

public class Main {

	public static void main(String[] args) throws Exception {
		Config config = new Config();
		Injector injector = Guice.createInjector(config);
		FixSchemaVolumesRun run = injector.getInstance(FixSchemaVolumesRun.class);
		run.run();
	}
}
