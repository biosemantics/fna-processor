package edu.arizona.biosemantics.fnaprocessor.action.parenthesis;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.inject.Inject;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;

/**
 * ParenthesisAction reports unclosed parenthesis validations inside the description elements
 * of the volume's files. A report on unclosed parenthesis is stored in
 * {volumeDir}/{filename}-unclosed-parenthesis.txt
 */
public class ParenthesisAction implements VolumeAction {

	private static final Logger logger = Logger.getLogger(ParenthesisAction.class);

	private BracketValidator bracketValidator;

	/**
	 * @param bracketValidator: The bracket validator to use
	 */
	@Inject
	public ParenthesisAction(BracketValidator bracketValidator) {
		this.bracketValidator = bracketValidator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(File volumeDir) throws Exception {
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());

		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			List<Element> descriptions = getDescriptions(file);
			for(Element description : descriptions) {
				if(!bracketValidator.validate(description.getText())) {
					StringWriter stringWriter = new StringWriter();
					outputter.output(description, stringWriter);

					try(PrintWriter out = new PrintWriter(
							new File(volumeDir, file.getName().replaceAll(".xml", "") + "-unclosed-parenthesis.txt"))) {
						out.println("Parenthesis missing in file: " + file.getName() +
								"Description element Number " + descriptions.indexOf(description));
						out.println("Content: " + stringWriter.toString());
						out.println("Missing parenthesis candidates: " + bracketValidator.getBracketCountDifferences(description.getText()));
					}

					logger.error("Parenthesis missing in file: " + file.getName() +
							"Description element Number " + descriptions.indexOf(description));
					logger.error("Content: " + stringWriter.toString());
					logger.error("Missing parenthesis candidates: " + bracketValidator.getBracketCountDifferences(description.getText()));

				}
			}
		}
	}

	/**
	 * Gets all descriptions of the file
	 * @param file: the file to get the descriptions from
	 * @return the list of description elements
	 */
	private List<Element> getDescriptions(File file) {
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document;
		try {
			document = saxBuilder.build(file);
		} catch (JDOMException | IOException e) {
			logger.error("SAXBuilder cannot build "+(file.getName())+ ".");
			return new ArrayList<Element>();
		}
		XPathFactory xPathFactory = XPathFactory.instance();
		XPathExpression<Element> descriptionsMatcher =
				xPathFactory.compile("//description", Filters.element(),
						null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		return descriptionsMatcher.evaluate(document);
	}

}
