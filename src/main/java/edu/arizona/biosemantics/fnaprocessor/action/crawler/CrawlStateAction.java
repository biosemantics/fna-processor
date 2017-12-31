package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import java.io.File;
import java.io.IOException;

public interface CrawlStateAction {

	void run(File volumeDir, CrawlState crawlState) throws IOException;

}
