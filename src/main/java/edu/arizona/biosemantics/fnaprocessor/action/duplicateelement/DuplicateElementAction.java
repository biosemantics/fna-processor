package edu.arizona.biosemantics.fnaprocessor.action.duplicateelement;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

/**
 * DuplicateElementAction checks if there are duplicate elements within a parent element
 * of a file. I.e. duplicate elements of a different parent are not considered duplicates.
 * If duplicate elements are found they are reported in {volumeDir}/{filename}-duplicate-elements.txt
 */
public class DuplicateElementAction implements VolumeAction {

	private static Logger logger = Logger.getLogger(DuplicateElementAction.class);

	private XMLOutputter outputter = new XMLOutputter();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(File volumeDir) throws JDOMException, IOException {
		logger.info("Finding duplicates elements for " + volumeDir.getAbsolutePath());

		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			logger.info(file.getName());
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(file);
			Element rootNode = document.getRootElement();

			Set<String> duplicates = findDuplicates(rootNode);

			if(!duplicates.isEmpty()) {
				try(PrintWriter out = new PrintWriter(
						new File(volumeDir, file.getName().replaceAll(".xml", "") + "-duplicate-elements.txt"))) {
					for(String duplicate : duplicates)
						out.println(duplicate);
				}
			}
		}
	}

	/**
	 * Finds duplicate elements within a parent
	 * @param parent: node for which to check the children for duplicates
	 * @return set containing element that were duplicated
	 */
	private Set<String> findDuplicates(Element parent) {
		Set<String> duplicates = new HashSet<String>();
		Set<String> seenElements = new HashSet<String>();
		for(Element child : parent.getChildren()) {
			String elementString = outputter.outputString(child);
			if(seenElements.contains(elementString))
				duplicates.add(elementString);
			seenElements.add(elementString);

			duplicates.addAll(this.findDuplicates(child));
		}

		return duplicates;
	}
}
