package edu.arizona.biosemantics.fnaprocessor.eflorascrawler.documentretrieval;

import org.jsoup.nodes.Document;

/**
 * Retrieves a document for a given url
 */
public interface DocumentRetriever {

	/**
	 * @param url: The url for which to retrieve the document
	 * @return the document for the given url
	 * @throws Exception if there was a problem retrieving the document
	 */
	public Document getDocument(String url) throws Exception;
}
