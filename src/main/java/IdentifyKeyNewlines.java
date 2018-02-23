import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import edu.arizona.biosemantics.fnaprocessor.Configuration;


public class IdentifyKeyNewlines {


	public static void main(String[] args) throws IOException {

		File volumesDir = new File(Configuration.fnaTextProcessingDirectory);
		File volumeDir = new File(volumesDir, "V25");

		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {

			fixXmlMalformedIssues(file);
			SAXBuilder saxBuilder = new SAXBuilder();
			org.jdom2.Document document;
			try {
				document = saxBuilder.build(file);

				XPathFactory xPathFactory = XPathFactory.instance();

				XPathExpression<Element> keyMatcher =
						xPathFactory.compile("/bio:treatment/key", Filters.element(),
								null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
				List<Element> keyElements = keyMatcher.evaluate(document);
				for(Element key : keyElements) {
					Element keyBody = key.getChild("key_body");
					if(keyBody != null) {
						String keyText = keyBody.getText();
						String[] lines = keyText.split("\n");
						for(String line : lines) {
							if(!line.matches("^\\d.*$")) {
								System.out.println(file.getName() + " " + line);
							}
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 *
	 * @param file
	 * @throws IOException
	 */
	private static void fixXmlMalformedIssues(File file) throws IOException {
		StringBuffer sb = new StringBuffer();
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			//boolean insideKey = false;
			while(br.ready()) {
				String line = br.readLine();

				if(line.contains("<bio:treatment>")) {
					line = line.replaceAll("<bio:treatment>",
							"<bio:treatment xmlns:bio=\"http://www.github.com/biosemantics\" " +
									"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
							"xsi:schemaLocation=\"http://www.github.com/biosemantics http://www.w3.org/2001/XMLSchema-instance\">");
				}

				line = line.replaceAll("&", "&amp;");

				line = line.replaceAll("(< 45°)", "(&lt; 45°)");

				line = line.replaceAll("\\[<=", "[&lt;=");

				sb.append(line + "\n");
				/*if(!insideKey && !line.contains("<key>")) {
					sb.append(line + "\n");
				}*/

				/*if(line.contains("<key>")) {
					insideKey = true;
				}
				if(line.contains("</key>")) {
					insideKey = false;
				}*/
			}
		}
		try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			writer.write(sb.toString());
		}
	}
}
