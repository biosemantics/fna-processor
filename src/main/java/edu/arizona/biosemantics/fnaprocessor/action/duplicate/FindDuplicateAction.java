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

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

/**
 * FindDuplicateAction checks for duplicate files in a volume directory by creating
 * a hash value of the files content. Found duplicate files are written to {volumeDir}/duplicates.txt.
 */
public class FindDuplicateAction implements VolumeAction {

	private static Logger logger = Logger.getLogger(FindDuplicateAction.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(File volumeDir) throws JDOMException, IOException {
		StringBuilder sb = new StringBuilder();
		logger.info("Finding duplicates for " + volumeDir.getAbsolutePath());
		Map<String, Set<File>> seenNumbers = new HashMap<String, Set<File>>();

		for(File inputFile : volumeDir.listFiles(new FileFilter() {
			@Override
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
				sb.append("Duplicates for number: " + number + "\n");
				for(File file : seenNumbers.get(number)) {
					sb.append(file.getAbsolutePath() + "\n");
					logger.info(file.getAbsolutePath());
				}
			}
		}

		String report = sb.toString().trim();
		if(!report.isEmpty()) {
			try(PrintWriter out = new PrintWriter(
					new File(volumeDir, "duplicates.txt"))) {
				out.println(sb.toString());
			}
		}
	}

	/**
	 * Returns a string that serves as the signature of the file
	 * In a first attempt the file number was used. Later it was replaced by the entire file content.
	 * @param file: the file for which to create a signature
	 * @return the signature
	 * @throws JDOMException if the file could not be parsed
	 * @throws IOException if the file could not be accessed
	 */
	private String getNumber(File file) throws JDOMException, IOException {
		byte[] data = Files.readAllBytes(file.toPath());
		String md5 = DigestUtils.md5Hex(data);
		return md5;

		/*SAXBuilder builder = new SAXBuilder();
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

}
