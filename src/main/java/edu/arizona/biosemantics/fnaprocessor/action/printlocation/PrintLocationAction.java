package edu.arizona.biosemantics.fnaprocessor.action.printlocation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.fnaprocessor.action.VolumeAction;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;

/**
 * PrintLocationAction outputs the location of the taxon in the print publication as
 * retrieved from the mapped eflora document. The print location is stored in the
 * XML element in the volume file.
 * {volumeDir}/{filename}-print-location.txt
 */
public class PrintLocationAction implements VolumeAction {

	private final static Logger logger = Logger.getLogger(PrintLocationAction.class);

	private MapStateProvider mapStateProvider;
	private CrawlStateProvider crawlStateProvider;
	private Map<File, String> volumeDirUrlMap;


	/**
	 * @param crawlStateProvider to use to retrieve crawled eflora documents
	 * @param mapStateProvider to find the eflora documents mapped to a volume file
	 * @param volumeDirUrlMap to find the eflora volume url for a given volume dir
	 */
	@Inject
	public PrintLocationAction(CrawlStateProvider crawlStateProvider,
			@Named("serializedMapStateProvider") MapStateProvider mapStateProvider,
			@Named("volumeDirUrlMap") Map<File, String> volumeDirUrlMap) {
		this.mapStateProvider = mapStateProvider;
		this.crawlStateProvider = crawlStateProvider;
		this.volumeDirUrlMap = volumeDirUrlMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(File volumeDir) throws Exception {
		logger.info("Running PrintLocationAction for " + volumeDir);
		MapState mapState = mapStateProvider.getMapState(volumeDir, new MapState(volumeDirUrlMap.get(volumeDir)));
		CrawlState crawlState = crawlStateProvider.getCrawlState(volumeDirUrlMap.get(volumeDir));

		for(File file : volumeDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			if(mapState.hasUrl(file)) {
				String url = mapState.getUrl(file);
				Document document = crawlState.getUrlDocumentMapping(url);

				Element locationElement = document.selectFirst("#footerTable > tbody > tr > td");
				if(locationElement != null) {
					String printLocation = locationElement.html();
					setPrintLocation(file, printLocation);
					/*try(PrintWriter out = new PrintWriter(
							new File(volumeDir, file.getName().replaceAll(".xml", "") + "-print-location.txt"))) {
						out.println(printLocation);
					}*/
				} else {
					logger.warn("Did not find print location for file " + file);
				}
			} else {
				logger.error("Missing file to document mapping for file " + file);
			}
		}
	}

	/**
	 * Sets the text of the /meta/source/author element in the file
	 * @param file: The file to set the author element's text
	 * @param text: The text to set
	 */
	private void setPrintLocation(File file, String text) {
		SAXBuilder saxBuilder = new SAXBuilder();
		org.jdom2.Document document;
		try {
			document = saxBuilder.build(file);
		} catch (JDOMException | IOException e) {
			logger.error("SAXBuilder cannot build "+(file.getName())+ ".");
			return;
		}

		XPathFactory xPathFactory = XPathFactory.instance();
		XPathExpression<org.jdom2.Element> metaMatcher =
				xPathFactory.compile("//meta", Filters.element(),
						null, Namespace.getNamespace("bio", "http://www.github.com/biosemantics"));
		org.jdom2.Element metaElement =  metaMatcher.evaluateFirst(document);
		if(metaElement != null) {
			String vol = getVolume(text);
			org.jdom2.Element otherInfoElement = new org.jdom2.Element("other_info_on_meta");
			otherInfoElement.setText(vol);
			otherInfoElement.setAttribute("type", "volume");
			metaElement.addContent(otherInfoElement);

			List<String> mentionPages = getMentionPages(text);
			for(String page : mentionPages) {
				otherInfoElement = new org.jdom2.Element("other_info_on_meta");
				otherInfoElement.setText(page);
				otherInfoElement.setAttribute("type", "mention_page");
				metaElement.addContent(otherInfoElement);
			}

			List<String> treatmentPages = getTreatmentPages(text);
			for(String page : treatmentPages) {
				otherInfoElement = new org.jdom2.Element("other_info_on_meta");
				otherInfoElement.setText(page);
				otherInfoElement.setAttribute("type", "treatment_page");
				metaElement.addContent(otherInfoElement);
			}

			List<String> illustrationPages = getIllustrationPages(text);
			for(String page : illustrationPages) {
				otherInfoElement = new org.jdom2.Element("other_info_on_meta");
				otherInfoElement.setText(page);
				otherInfoElement.setAttribute("type", "illustration_page");
				metaElement.addContent(otherInfoElement);
			}
			writeToFile(document, file);
		}
	}

	public static void main(String[] args) {
		String text = "FNA Vol. 20 Page 3,9, 11, 12, <i>14</i>, 17, 36, 204, <b>256</b>, 257, 334";
		System.out.println(getVolume(text));
		System.out.println(getMentionPages(text));
		System.out.println(getTreatmentPages(text));
		System.out.println(getIllustrationPages(text));
	}

	private static String getVolume(String text) {
		String volume = text.replaceAll("FNA Vol. ", "");
		return volume.split(" ")[0];
	}

	private static List<String> getMentionPages(String text) {
		List<String> result = new ArrayList<String>();
		if(text.contains(" Page ")) {
			String pagesText = text.split(" Page ")[1];
			String pages[] = pagesText.split(",");
			for(String page : pages) {
				page = page.trim();
				if(page.startsWith("<b>")) {
					page = page.replaceAll("<b>", "");
					page = page.replaceAll("</b>", "");
					continue;
				} else if(page.startsWith("<i>")) {
					page = page.replaceAll("<i>", "");
					page = page.replaceAll("</i>", "");
					continue;
				} else {
					result.add(page);
				}
			}
		}
		return result;
	}

	private static List<String> getTreatmentPages(String text) {
		List<String> result = new ArrayList<String>();
		if(text.contains(" Page ")) {
			String pagesText = text.split(" Page ")[1];
			String pages[] = pagesText.split(",");
			for(String page : pages) {
				page = page.trim();
				if(page.startsWith("<b>")) {
					page = page.replaceAll("<b>", "");
					page = page.replaceAll("</b>", "");
					result.add(page);
				} else if(page.startsWith("<i>")) {
					page = page.replaceAll("<i>", "");
					page = page.replaceAll("</i>", "");
					continue;
				} else {
					continue;
				}
			}
		}
		return result;
	}

	private static List<String> getIllustrationPages(String text) {
		List<String> result = new ArrayList<String>();
		if(text.contains(" Page ")) {
			String pagesText = text.split(" Page ")[1];
			String pages[] = pagesText.split(",");
			for(String page : pages) {
				page = page.trim();
				if(page.startsWith("<b>")) {
					page = page.replaceAll("<b>", "");
					page = page.replaceAll("</b>", "");
					continue;
				} else if(page.startsWith("<i>")) {
					page = page.replaceAll("<i>", "");
					page = page.replaceAll("</i>", "");
					result.add(page);
				} else {
					continue;
				}
			}
		}
		return result;
	}

	/**
	 * Stores the @see org.jdom2.Document to file
	 * @param document to store
	 * @param file: where to store the document
	 */
	private void writeToFile(org.jdom2.Document document, File file) {
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			outputter.output(document, bw);
		} catch (IOException e) {
			logger.warn("IO Error writing update XML to file", e);
		}
	}

}
