package edu.arizona.biosemantics.fnaprocessor.action.schemacheck;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XMLValidator validates a XML file against a XML schema
 */
public class XMLValidator {

	private static final Logger logger = Logger.getLogger(XMLValidator.class);

	private File schemaFile;
	private URL url;
	private String invalidMessage = "";

	public XMLValidator(File schemaFile) {
		this.schemaFile = schemaFile;
	}

	public XMLValidator(URL url) {
		this.url = url;
	}

	public boolean validate(String input) {
		Source schemaSource = null;
		if(schemaFile != null) {
			schemaSource = new StreamSource(schemaFile);
			return validate(input, schemaSource);
		} else if(url != null) {
			try(InputStream inputStream = url.openStream()) {
				schemaSource = new StreamSource(inputStream);
				return validate(input, schemaSource);
			} catch (IOException e) {
				logger.error("Couldn't open or close input stream from url", e);
				invalidMessage = "No XML schema available.";
				return false;
			}
		}
		invalidMessage = "No XML schema available.";
		return false;
	}

	private boolean validate(String input, Source schemaSource) {
		if(schemaSource != null) {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = null;
			try {
				schema = factory.newSchema(schemaSource);
			} catch (SAXException e) {
				logger.error("Couldn't create schema", e);
			}
			if(schema != null) {
				final List<SAXParseException> exceptions = new LinkedList<SAXParseException>();
				Validator validator = schema.newValidator();
				validator.setErrorHandler(new ErrorHandler() {
					@Override
					public void warning(SAXParseException exception)
							throws SAXException {
						exceptions.add(exception);
					}
					@Override
					public void fatalError(SAXParseException exception)
							throws SAXException {
						exceptions.add(exception);
					}
					@Override
					public void error(SAXParseException exception)
							throws SAXException {
						exceptions.add(exception);
					}
				});

				try(ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes("UTF-8"))) {
					validator.validate(new StreamSource(inputStream));
					if(!exceptions.isEmpty()) {
						for(SAXParseException saxParseException : exceptions) {
							invalidMessage = "Line: " + saxParseException.getLineNumber() + "; Column: " + saxParseException.getColumnNumber() +
									"; " + saxParseException.getMessage() + "\n";
						}
						invalidMessage = invalidMessage.substring(0, invalidMessage.length() - 1);
						return false;
					} else {
						invalidMessage = "";
						return true;
					}
				} catch(SAXException e) {
					logger.error("Validation of a input failed", e);
					invalidMessage = e.getMessage();
					return false;
				} catch (UnsupportedEncodingException e) {
					logger.error("Encoding not supported", e);
					invalidMessage = "Input could not be read.";
					return false;
				} catch (IOException e) {
					logger.error("Input could not be read.", e);
					invalidMessage = "Input could not be read.";
					return false;
				}
			} else {
				invalidMessage = "No XML schema available.";
				return false;
			}
		} else {
			invalidMessage = "No XML schema available.";
			return false;
		}
	}

	public String getInvalidMessage() {
		return invalidMessage;
	}

	public static void main(String[] args) throws IOException {
		File file = new File("C:\\etcsitebase\\etcsite\\data\\textCapture\\charaparser\\387\\out\\Fernald_Rosaceae_1950.xml");
		byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		String fileContent = new String(bytes, Charset.forName("UTF8"));
		//System.out.println(fileContent);

		XMLValidator taxonDescriptionValidator = new XMLValidator(new File("semanticMarkupOutput.xsd"));
		//XMLValidator xmlValidator = new XMLValidator("http://raw.githubusercontent.com/biosemantics/schemas/0.0.1/semanticMarkupInput.xsd;https://raw.githubusercontent.com/biosemantics/schemas/master/semanticMarkupOutput.xsd");

		System.out.println(taxonDescriptionValidator.validate(fileContent));
		System.out.println(taxonDescriptionValidator.getInvalidMessage());
		//validate();
	}

	/*public static void validate() throws IOException {
		StreamSource schemaSource = new StreamSource(new File("semanticMarkupInput.xsd"));
		File xml = new File("3.xml");
        final List<SAXParseException> exceptions = new LinkedList<SAXParseException>();
	    try {
	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema(schemaSource);
	        Validator validator = schema.newValidator();
	        validator.setErrorHandler(new ErrorHandler()
	        {
	          @Override
	          public void warning(SAXParseException exception) throws SAXException
	          {
	            exceptions.add(exception);
	          }

	          @Override
	          public void fatalError(SAXParseException exception) throws SAXException
	          {
	            exceptions.add(exception);
	          }

	          @Override
	          public void error(SAXParseException exception) throws SAXException
	          {
	            exceptions.add(exception);
	          }
	        });
	        StreamSource xmlFile = new StreamSource(xml);
	        validator.validate(xmlFile);
	        if(!exceptions.isEmpty()) {
	        	System.out.println("invalid");
	        	 for(SAXParseException saxParseException : exceptions) {
	 	        	System.out.println(saxParseException.getLineNumber());
	 	        	System.out.println(saxParseException.getColumnNumber());
	 	        	System.out.println(saxParseException.getMessage());
	 	        	System.out.println(saxParseException.getLocalizedMessage());
	 	        }
	        } else {
	        	System.out.println("valid");
	        }
	    } catch(SAXException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}*/

}
