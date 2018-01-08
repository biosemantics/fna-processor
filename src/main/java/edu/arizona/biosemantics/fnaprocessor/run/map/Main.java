package edu.arizona.biosemantics.fnaprocessor.run.map;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {

	public static void main(String[] args) throws Exception {
		Config config = new Config();
		Injector injector = Guice.createInjector(config);
		MapRun run = injector.getInstance(MapRun.class);
		run.run();
	}
}
