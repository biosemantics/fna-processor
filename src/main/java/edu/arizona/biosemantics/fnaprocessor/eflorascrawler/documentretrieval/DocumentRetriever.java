package edu.arizona.biosemantics.fnaprocessor.eflorascrawler.documentretrieval;

import org.jsoup.nodes.Document;

public interface DocumentRetriever {
	
	public Document getDocument(String url) throws Exception;
}
