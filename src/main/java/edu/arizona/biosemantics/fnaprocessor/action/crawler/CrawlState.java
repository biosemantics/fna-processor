package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CrawlState implements Serializable {

	private Map<String, Document> urlDocumentMapping = new HashMap<String, Document>();
	private Map<Document, String> reverseUrlDocumentMapping = new HashMap<Document, String>();
	private Map<String, String> urlToLinkNameMapping = new HashMap<String, String>();
	private Map<String, Set<String>> targetToSourceLinksMapping = new HashMap<String, Set<String>>();
	private List<String> unmappedUrls = new ArrayList<String>();
	private Map<File, Document> fileToDocumentMapping = new HashMap<File, Document>();
	
	public List<String> getUnmappedUrls() {
		return this.unmappedUrls;
	}
	
	public String getLinkName(String url) {
		return this.urlToLinkNameMapping.get(url);
	}

	public List<File> getMappedFiles() {
		return new ArrayList<File>(this.fileToDocumentMapping.keySet());
	}

	public Map<File, Document> getFileToDocumentMaping() {
		return this.fileToDocumentMapping;
	}

	public void reset() {
		this.fileToDocumentMapping = new HashMap<File, Document>();
		this.unmappedUrls = new ArrayList<String>();
		this.urlToLinkNameMapping = new HashMap<String, String>();
		this.urlDocumentMapping = new HashMap<String, Document>();
		this.reverseUrlDocumentMapping = new HashMap<Document, String>();
		this.targetToSourceLinksMapping = new HashMap<String, Set<String>>();
	}


	public void putFileToDocumentMapping(File file, Document document) {
		this.fileToDocumentMapping.put(file, document);
	}

	public void putTargetToSourceLinksMapping(String volumeUrl, HashSet<String> set) {
		this.targetToSourceLinksMapping.put(volumeUrl, set);
	}

	public Set<String> getTargetToSourceLinksMapping(String url) {
		return this.targetToSourceLinksMapping.get(url);
	}

	public void putUrlToLinkNameMapping(String url, String name) {
		this.urlToLinkNameMapping.put(url, name);
	}

	public boolean containsTargetToSourceLinksMapping(String href) {
		return this.targetToSourceLinksMapping.containsKey(href);
	}

	public void addUnmappedUrls(String url) {
		this.unmappedUrls.add(url);
	}
	
	public String getUrl(Document document) {
		return this.reverseUrlDocumentMapping.get(document);
	}
	
	public Document getDocument(File file) {
		return fileToDocumentMapping.get(file);
	}

	public boolean hasDocument(File file) {
		return this.fileToDocumentMapping.containsKey(file);
	}

	public boolean containsUrlDocumentMapping(String url) {
		return this.urlDocumentMapping.containsKey(url);
	}

	public Document getUrlDocumentMapping(String url) {
		return this.urlDocumentMapping.get(url);
	}

	public void putUrlDocumentMapping(String url, Document document) {
		this.urlDocumentMapping.put(url, document);
	}

	public void putReverseUrlDocumentMapping(Document document, String url) {
		this.reverseUrlDocumentMapping.put(document, url);
	}

	public int getUrlDocumentSize() {
		return this.urlDocumentMapping.size();
	}

	public Set<String> getUrlDocumentKeys() {
		return this.urlDocumentMapping.keySet();
	}
	
    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(this.targetToSourceLinksMapping);
        stream.writeObject(this.urlToLinkNameMapping);
        stream.writeObject(this.unmappedUrls);
    
        Map<File, String> serializableFileDocumentMapping = new HashMap<File, String>();
        for(File file : this.fileToDocumentMapping.keySet()) {
        	serializableFileDocumentMapping.put(file, fileToDocumentMapping.get(file).toString());
        }
        stream.writeObject(serializableFileDocumentMapping);
        Map<String, String> serializableUrlDocumentMapping = new HashMap<String, String>();
        for(String url : this.urlDocumentMapping.keySet()) {
        	serializableUrlDocumentMapping.put(url, urlDocumentMapping.get(url).toString());
        }
        stream.writeObject(serializableUrlDocumentMapping);
        Map<String, String> serializableReverseUrlDocumentMapping = new HashMap<String, String>();
        for(Document document : this.reverseUrlDocumentMapping.keySet()) {
        	serializableReverseUrlDocumentMapping.put(document.toString(), this.reverseUrlDocumentMapping.get(document));
        }
        stream.writeObject(serializableReverseUrlDocumentMapping);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
    	targetToSourceLinksMapping = (Map<String, Set<String>>) stream.readObject();
    	urlToLinkNameMapping = (Map<String, String>) stream.readObject();
    	unmappedUrls = (List<String>) stream.readObject();
    	
    	urlDocumentMapping = new HashMap<String, Document>();
    	reverseUrlDocumentMapping = new HashMap<Document, String>();
    	fileToDocumentMapping = new HashMap<File, Document>();
    	
        Map<File, String> serializableFileDocumentMapping = (Map<File, String>)stream.readObject();
        for(File file : serializableFileDocumentMapping.keySet()) {
        	this.fileToDocumentMapping.put(file, Jsoup.parse(serializableFileDocumentMapping.get(file)));
        }
        Map<String, String> serializableUrlDocumentMapping =  (Map<String, String>)stream.readObject();
        for(String url : serializableUrlDocumentMapping.keySet()) {
        	this.urlDocumentMapping.put(url, Jsoup.parse(serializableUrlDocumentMapping.get(url)));
        }
        Map<String, String> serializableReverseUrlDocumentMapping = (Map<String, String>)stream.readObject();
        for(String document : serializableReverseUrlDocumentMapping.keySet()) {
        	this.reverseUrlDocumentMapping.put(Jsoup.parse(document), 
        			serializableReverseUrlDocumentMapping.get(document));
        }
    }

	
	
}
