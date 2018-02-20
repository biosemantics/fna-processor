package edu.arizona.biosemantics.fnaprocessor.eflorascrawler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * CrawlState stores the crawled data from eflora for a specific volume
 * More specifically, it keeps a
 * - url to document map
 * - url to link name map
 * - url to link text map (link name is the bolded portion of the overall link text)
 * - target to source link map (i.e. a hyperlink target url to source url map)
 * - the volume url
 */
public class CrawlState implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4187078225958672835L;
	/**
	 * url to document map
	 */
	private Map<String, String> urlDocumentMap = new HashMap<String, String>();
	/**
	 * url to link name map
	 */
	private Map<String, String> urlToLinkNameMap = new HashMap<String, String>();
	/**
	 * url to link text map
	 */
	private Map<String, String> urlToLinkTextMap = new HashMap<String, String>();
	/**
	 * target to source link map
	 */
	private Map<String, Set<String>> targetToSourceLinksMap = new HashMap<String, Set<String>>();
	/**
	 * volume url
	 */
	private String volumeUrl;

	/**
	 * @param volumeUrl: The volume url of the volume of this crawl state
	 */
	public CrawlState(String volumeUrl) {
		this.volumeUrl = volumeUrl;
	}

	/**
	 * @param url: The url for which to retrieve the link name
	 * @return the link name of the url
	 */
	public String getLinkName(String url) {
		return this.urlToLinkNameMap.get(url);
	}

	/**
	 * @param url: the url for which to retrieve the link text
	 * @return the link text of the url
	 */
	public String getLinkText(String url) {
		return this.urlToLinkTextMap.get(url);
	}

	/**
	 * Resets the crawl state, effectively resetting all mapps
	 */
	public void reset() {
		this.urlToLinkNameMap = new HashMap<String, String>();
		this.urlToLinkTextMap = new HashMap<String, String>();
		this.urlDocumentMap = new HashMap<String, String>();
		this.targetToSourceLinksMap = new HashMap<String, Set<String>>();
	}

	/**
	 * Sets the incoming hyperlinks for the given volumeUrl
	 * @param volumeUrl: The target of the hyperlinks to store
	 * @param set: The set of source urls that hyperlink to the target volumeUrl
	 */
	public void putTargetToSourceLinksMapping(String volumeUrl, HashSet<String> set) {
		this.targetToSourceLinksMap.put(volumeUrl, set);
	}

	/**
	 * @param url: The url for which to get the incoming hyperlinks
	 * @return the set of incoming hyperlinks
	 */
	public Set<String> getTargetToSourceLinksMapping(String url) {
		return this.targetToSourceLinksMap.get(url);
	}

	/**
	 * Sets the link name and text for a url
	 * @param url: The url for which to set the link name and text
	 * @param name: The link name to set
	 * @param linkText: The link text to set
	 */
	public void putUrlToLinkNameMapping(String url, String name, String linkText) {
		this.urlToLinkNameMap.put(url, name);
		this.urlToLinkTextMap.put(url, linkText);
	}

	/**
	 * @param targetUrl: The url for which to check for incoming hyperlink
	 * @return whether there are hyperlinks stored for the target url
	 */
	public boolean containsTargetToSourceLinksMapping(String targetUrl) {
		return this.targetToSourceLinksMap.containsKey(targetUrl);
	}

	/**
	 * @param url: The url for which to check for an existing document
	 * @return whether there is a document stored for the given url
	 */
	public boolean containsUrlDocumentMapping(String url) {
		return this.urlDocumentMap.containsKey(url);
	}

	/**
	 * @param url: The url for which to get the document
	 * @return the document stored for the url
	 */
	public Document getUrlDocumentMapping(String url) {
		return Jsoup.parse(this.urlDocumentMap.get(url));
	}

	/**
	 * Sets the document for the given url
	 * @param url: The url for which to set the document
	 * @param document: The document to set for the url
	 */
	public void putUrlDocumentMapping(String url, Document document) {
		this.urlDocumentMap.put(url, document.toString());
	}

	/**
	 * @return the urls stored in this crawlState
	 */
	public Set<String> getUrls() {
		return this.urlDocumentMap.keySet();
	}

	/**
	 * @return the volume url of this crawlState
	 */
	public String getVolumeUrl() {
		return volumeUrl;
	}

}
