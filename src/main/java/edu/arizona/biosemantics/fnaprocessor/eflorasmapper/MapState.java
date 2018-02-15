package edu.arizona.biosemantics.fnaprocessor.eflorasmapper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * MapState stores the mapping information from volume files to volume urls on eflora
 * More specifically, it keeps a
 * - file to url map
 * - url to file map
 * - url to class map (to store which mapper resolved this mapping)
 * - volume url of this state
 */
public class MapState implements Serializable {

	private Map<File, String> fileUrlMap = new HashMap<File, String>();
	private Map<String, File> urlFileMap = new HashMap<String, File>();
	private Map<File, Class<?>> fileMapper = new HashMap<File, Class<?>>();
	private String volumeUrl;

	/**
	 * @param volumeUrl: The volume url of the state
	 */
	public MapState(String volumeUrl) {
		this.volumeUrl = volumeUrl;
	}

	/**
	 * @return a list of mapped files
	 */
	public List<File> getMappedFiles() {
		return new ArrayList<File>(this.fileUrlMap.keySet());
	}

	/**
	 * @return the file to url mapping
	 */
	public Map<File, String> getFileToDocumentMaping() {
		return this.fileUrlMap;
	}

	/**
	 * resets the mapping state
	 */
	public void reset() {
		this.fileUrlMap = new HashMap<File, String>();
		this.urlFileMap = new HashMap<String, File>();
		this.fileMapper = new HashMap<File, Class<?>>();
	}

	/**
	 * Sets a mapping from file to url for a given mapper
	 * @param file: The file for which to set a url
	 * @param url: the url to set for the file
	 * @param mapper: The mapper used to create this mapping
	 */
	public void putFileUrlMap(File file, String url, Class<?> mapper) {
		this.fileUrlMap.put(file, url);
		this.urlFileMap.put(url, file);
		this.fileMapper.put(file, mapper);
	}

	/**
	 * @param file: the file for which to get the mapped url
	 * @return the url mapped to the file
	 */
	public String getUrl(File file) {
		return fileUrlMap.get(file);
	}

	/**
	 * @param file: The file for which to check if there is a mapped url
	 * @return if the file is mapped to a url
	 */
	public boolean hasUrl(File file) {
		return this.fileUrlMap.containsKey(file);
	}

	/**
	 * @return the volume url of the state
	 */
	public String getVolumeUrl() {
		return volumeUrl;
	}

	/**
	 * @param url: The url for which to check if is mapped to a file
	 * @return if the url is mapped to a file
	 */
	public boolean hasFile(String url) {
		return this.urlFileMap.containsKey(url);
	}

	/**
	 * @param url: The url for which to get the mapped file
	 * @return the file mapped to the url
	 */
	public File getFile(String url) {
		return this.urlFileMap.get(url);
	}

	/**
	 * @param file: The file for which to get the mapper
	 * @return the mapper used to mape the file
	 */
	public Class<?> getMapper(File file) {
		return this.fileMapper.get(file);
	}
}
