package edu.arizona.biosemantics.fnaprocessor.action.schema;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

public class SchemaAction implements VolumeAction {

	private static final Logger logger = Logger.getLogger(SchemaAction.class);
	
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Fix Schema for volume " + volumeDir);
		
		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			addSchema(file, "bio", "http://www.github.com/biosemantics");
			addSchema(file, "xsi", "http://www.w3.org/2001/XMLSchema-instance");
		}
	}

	private static void addSchema(File file, String prefix, String namespace) throws Exception {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(false);
	    DocumentBuilder builder = factory.newDocumentBuilder();
		
	    InputStream inputStream= new FileInputStream(file);
	    Reader reader = new InputStreamReader(inputStream, "UTF-8");
	    InputSource is = new InputSource(reader);
	    is.setEncoding("UTF-8");
	    Document document = builder.parse(is);
		
		// Upgrade the DOM level 1 to level 2 with the correct namespace
		Element originalDocumentElement = document.getDocumentElement();
		Element newDocumentElement = document.createElementNS(namespace, originalDocumentElement.getNodeName());
		// Set the desired namespace and prefix
		newDocumentElement.setPrefix(prefix);
		// Copy all children
		NodeList list = originalDocumentElement.getChildNodes();
		while(list.getLength()!=0) {
		    newDocumentElement.appendChild(list.item(0));
		}
		// Replace the original element
		document.replaceChild(newDocumentElement, originalDocumentElement);
		
		DOMSource source = new DOMSource(document);
	    FileWriter writer = new FileWriter(new File(file.getName() + ".xml"));
	    StreamResult result = new StreamResult(writer);

	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.transform(source, result);
	}
	
	public static void main(String[] args) throws Exception {
		File file = new File("C:\\Users\\updates\\git\\FNATextProcessing\\V24\\1.xml");
		addSchema(file, "bio", "http://www.github.com/biosemantics");
	}
}
