package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.name.NameBasedVolumeMapper;

public class MapState implements Serializable {

	private Map<File, String> fileUrlMap = new HashMap<File, String>();
	private Map<String, File> urlFileMap = new HashMap<String, File>();
	private Map<File, Class<?>> fileMapper = new HashMap<File, Class<?>>();
	private String volumeUrl;
	
	public MapState(String volumeUrl) {
		this.volumeUrl = volumeUrl;
	}
	
	public List<File> getMappedFiles() {
		return new ArrayList<File>(this.fileUrlMap.keySet());
	}

	public Map<File, String> getFileToDocumentMaping() {
		return this.fileUrlMap;
	}
	
	public void reset() {
		this.fileUrlMap = new HashMap<File, String>();
		this.urlFileMap = new HashMap<String, File>();
	}
	
	public void putFileUrlMap(File file, String url, Class<?> mapper) {
		this.fileUrlMap.put(file, url);
		this.urlFileMap.put(url, file);
		this.fileMapper.put(file, mapper);
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
	
	public boolean hasFile(String url) {
		return this.urlFileMap.containsKey(url);
	}

	public File getFile(String url) {
		return this.urlFileMap.get(url);
	}

	public Class<?> getMapper(File file) {
		return this.fileMapper.get(file);
	}
}
