package edu.arizona.biosemantics.fnaprocessor.action.schema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlNamespaceManager {

	private Namespace bioNamespace = Namespace.getNamespace("bio", "http://www.github.com/biosemantics");
	private Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	
	public String getSchema(File file) {
		Document doc = null;
		try {
			SAXBuilder sax = new SAXBuilder();
			doc = sax.build(file);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		if(doc != null)
			return getSchema(doc);
		return null;
	}
	
	private String getSchema(Document doc) {
		Element rootElement = doc.getRootElement();
		return rootElement.getAttributeValue("schemaLocation", xsiNamespace).replace("http://www.github.com/biosemantics", "").trim();
	}
	
	public String getSchema(String fileContent) {
		try (StringReader reader = new StringReader(fileContent)) {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(reader);
			return getSchema(doc);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void setXmlSchema(File file) {
		Document doc = null;
		try {
			SAXBuilder sax = new SAXBuilder();
			doc = sax.build(file);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		if(doc != null) { 
			setXmlSchema(doc);
			try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
				try {
					XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
					xmlOutputter.output(doc, fileOutputStream);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setXmlSchema(Document doc) {
		String schemaUrl = "https://raw.githubusercontent.com/biosemantics/schemas/master/semanticMarkupInput.xsd";
		Element rootElement = doc.getRootElement();
		rootElement.setNamespace(bioNamespace);
		rootElement.addNamespaceDeclaration(bioNamespace);
		rootElement.addNamespaceDeclaration(xsiNamespace);
		rootElement.setAttribute("schemaLocation", "http://www.github.com/biosemantics " + schemaUrl, xsiNamespace);
	}

	public String setXmlSchema(String content) {
		try(StringReader reader = new StringReader(content)) {
			Document doc = null;
			try {
				SAXBuilder sax = new SAXBuilder();
				doc = sax.build(reader);
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
			}
			if(doc != null) {
				setXmlSchema(doc);
				
				try(StringWriter stringWriter = new StringWriter()) {
					try {
						XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
						xmlOutputter.output(doc, stringWriter);
					} catch (IOException e) {
						e.printStackTrace();
					}
					stringWriter.flush();
					String result = stringWriter.toString();
					return result;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public void removeXmlSchema(File file) {
		Document doc = null;
		try {
			SAXBuilder sax = new SAXBuilder();
			doc = sax.build(file);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		if(doc != null) {
			Element rootElement = doc.getRootElement();
			rootElement.setNamespace(null);
			rootElement.removeNamespaceDeclaration(bioNamespace);
			rootElement.removeNamespaceDeclaration(xsiNamespace);
			rootElement.removeAttribute("schemaLocation", xsiNamespace);
			try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
				try {
					XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
					xmlOutputter.output(doc, fileOutputStream);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}
