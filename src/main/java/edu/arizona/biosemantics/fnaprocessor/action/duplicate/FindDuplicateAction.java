package edu.arizona.biosemantics.fnaprocessor.action.duplicate;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

public class FindDuplicateAction implements VolumeAction {
	
	private static Logger logger = Logger.getLogger(FindDuplicateAction.class);

	@Override
	public void run(File volumeDir) throws JDOMException, IOException {
		StringBuilder report = new StringBuilder();
		logger.info("Finding duplicates for " + volumeDir.getAbsolutePath());
		Map<String, Set<File>> seenNumbers = new HashMap<String, Set<File>>();
		
		for(File inputFile : volumeDir.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.isFile() && file.getName().endsWith(".xml");
				}
			})) {
			
			try {
				String number = getNumber(inputFile);
				if(number == null) {
					logger.warn("File " + inputFile + " does not contain the number element");
				} else {
					if(!seenNumbers.containsKey(number))
						seenNumbers.put(number, new HashSet<File>());
					seenNumbers.get(number).add(inputFile);	
				}
			} catch(JDOMException e) {
				logger.error("Could not read number", e);
			}
		}
		
		for(String number : seenNumbers.keySet()) {
			if(seenNumbers.get(number).size() > 1) {
				logger.info("Duplicates for number: " + number);
				report.append("Duplicates for number: " + number + "\n");
				for(File file : seenNumbers.get(number)) {
					report.append(file.getAbsolutePath() + "\n");
					logger.info(file.getAbsolutePath());
				}
			}
		}
		
		try(PrintWriter out = new PrintWriter(
				new File(volumeDir, "duplicates.txt"))) {
		    out.println(report.toString());
		}
	}

	private String getNumber(File file) throws JDOMException, IOException {
		byte[] data = Files.readAllBytes(file.toPath());
		String md5 = DigestUtils.md5Hex(data);
		return md5;
		
		/*SAXBuilder builder = getSAXBuilder();
		Document document = (Document) builder.build(file);
		Element rootNode = document.getRootElement();
		rootNode.setNamespace(Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		if(rootNode.getChild("number") == null) {
			return null;
		} else {
			String numberField =  rootNode.getChild("number").getText();
			String number = numberField.trim();
			number = number.endsWith(".") ? number.replace(".", "") : number;
			return number;
		}*/
	}
	
	
    /*private static final SAXHandlerFactory FACTORY = new SAXHandlerFactory() {
       @Override
        public SAXHandler createSAXHandler(JDOMFactory factory) {
            return new SAXHandler() {
                @Override
                public void startElement(
                        String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
                    super.startElement("http://www.github.com/biosemantics", localName, qName, atts);
                }
                @Override
                public void startPrefixMapping(String prefix, String uri) throws SAXException {
                    return;
                }
            };
        }
    };*/


    /** Get a {@code SAXBuilder} that ignores namespaces.
     * Any namespaces present in the xml input to this builder will be omitted from the resulting {@code Document}. */
    public static SAXBuilder getSAXBuilder() {
        // Note: SAXBuilder is NOT thread-safe, so we instantiate a new one for every call.
        SAXBuilder saxBuilder = new SAXBuilder();
        //saxBuilder.setSAXHandlerFactory(FACTORY);
        return saxBuilder;
    }
}
