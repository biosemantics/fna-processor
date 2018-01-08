package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapState implements Serializable {

	private List<String> unmappedUrls = new ArrayList<String>();
	private Map<File, String> fileUrlMap = new HashMap<File, String>();
	private String volumeUrl;
	
	public MapState(String volumeUrl) {
		this.volumeUrl = volumeUrl;
	}

	public List<String> getUnmappedUrls() {
		return this.unmappedUrls;
	}
	
	public List<File> getMappedFiles() {
		return new ArrayList<File>(this.fileUrlMap.keySet());
	}

	public Map<File, String> getFileToDocumentMaping() {
		return this.fileUrlMap;
	}
	
	public void reset() {
		this.fileUrlMap = new HashMap<File, String>();
		this.unmappedUrls = new ArrayList<String>();
	}

	public void putFileUrlMap(File file, String url) {
		this.fileUrlMap.put(file, url);
	}

	public void addUnmappedUrls(String url) {
		this.unmappedUrls.add(url);
	}

	
	public String getUrl(File file) {
		return fileUrlMap.get(file);
	}

	public boolean hasUrl(File file) {
		return this.fileUrlMap.containsKey(file);
	}

	public String getVolumeUrl() {
		return volumeUrl;
	}
}
