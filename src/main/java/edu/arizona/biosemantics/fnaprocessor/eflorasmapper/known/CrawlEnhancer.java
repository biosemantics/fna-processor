package edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import edu.arizona.biosemantics.common.taxonomy.Rank;
import edu.arizona.biosemantics.fnaprocessor.Configuration;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CrawlEnhancer {
	
	private static Logger logger = Logger.getLogger(CrawlEnhancer.class);
	
	public static void main(String[] args) throws FileNotFoundException, IOException, JDOMException {
		String baseUrl = "http://www.efloras.org/";
		File knownDir = new File("post-run");
		int[] volumes = new int[] {
				19 };//,3,4,5,6,7,8,9,19,22,23,24,25,26,27,28 };
		Map<File, String> volumeDirUrlMap = new LinkedHashMap<File, String>();
		Map<Integer, File> volumeDirMap = new LinkedHashMap<Integer, File>();
		
		for(int volume : volumes) {
			String volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=10" + String.format("%02d", volume) + "&flora_id=1";
			File volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
			switch(volume) {
			case 2:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
				break;
			case 3:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
				break;
			case 19:
				//volume 19 is for 19-20-21 volumes since they are managed under one and the same url on efloras
				if(volume == 19) {
					volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=1019&flora_id=1";
					volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V19-20-21");
				}
				break;
			case 22:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
			}
			
			volumeDirMap.put(volume, volumeDir);
			volumeDirUrlMap.put(volumeDir, volumeUrl);
		}
		
		
		for(int volume : volumes) {
			File volumeFile = new File(knownDir, "known-v" + volume + ".csv");
			File volumeDir = volumeDirMap.get(volume);
			
			List<String[]> lines = new ArrayList<String[]>();
			try(CSVReader reader = new CSVReader(new FileReader(volumeFile))) {
				lines = reader.readAll();
				for(String[] line : lines) {
					if(line[0].trim().isEmpty())
						continue;
					
					String fileName = "";
					if(line[0].contains(")")) {
						fileName = line[0].split("\\)")[1].trim();
					}
					
					if(line[1].trim().isEmpty()) {
						boolean found = false;
						
						String searchName = createSearchName(new File(volumeDir, fileName));
						Document searchDoc = Jsoup.connect("http://www.efloras.org/browse.aspx?"
								+ "flora_id=1&name_str=" + searchName + "&btnSearch=Search").get();
						Elements taxonListRows = searchDoc.select("#ucFloraTaxonList_panelTaxonList > span > table > tbody > tr");
						for(org.jsoup.nodes.Element row : taxonListRows) {
							String volumeLabel = row.children().last().text().trim().replaceAll("\\s+", " ");
							if(volumeLabel.equalsIgnoreCase("FNA Vol. " + volume) || 
									(volume == 19 && (
											volumeLabel.startsWith("FNA Vol. " + volume) || volumeLabel.startsWith("FNA Vol. 20") ||
											volumeLabel.startsWith("FNA Vol. 21")))) {
								org.jsoup.nodes.Element a = row.selectFirst("a[title=\"Accepted Name\"]");
								String href = a.attr("href");
								//Document taxonDoc = Jsoup.connect(baseUrl + href).get();
								line[1] = "(" + searchName + ") " + baseUrl + href;
								found = true;
								logger.info("Found for: " + fileName + " searchName: " + searchName);
							}
						}
						
						if(!found) {	
							logger.warn("Not found for: " + fileName + " searchName: " + searchName);
						}
					}
				}
			}
			
			File updatedFile = new File(volumeFile.getParent(), "updated_" + volumeFile.getName());
			try(CSVWriter writer = new CSVWriter(new FileWriter(updatedFile))) {			
				writer.writeAll(lines);
			}
		}
	}
	

	static Comparator<Element> rankComparator = new Comparator<Element>() {
		@Override
		public int compare(Element o1, Element o2) {
			return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() - 
					Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
		}	
	};

	private static String createSearchName(File file) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		org.jdom2.Document document = (org.jdom2.Document) builder.build(file);
		
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> acceptedNameExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element());
		
		List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
		acceptedNameElements.sort(rankComparator);
		
		StringBuffer sb = new StringBuffer();
		
		if(acceptedNameElements.get(acceptedNameElements.size() - 1).getAttributeValue("rank").equalsIgnoreCase("subfamily")) {
			if(acceptedNameElements.get(0).getAttributeValue("rank").equalsIgnoreCase("family")) {
				return acceptedNameElements.get(0).getText() + " subfam. " + acceptedNameElements.get(acceptedNameElements.size() - 1).getText();
			}
		}
		
		if(acceptedNameElements.get(acceptedNameElements.size() - 1).getAttributeValue("rank").equalsIgnoreCase("genus")) {
			return acceptedNameElements.get(acceptedNameElements.size() - 1).getText();
		}
		
		for(Element el : acceptedNameElements) {
			if(acceptedNameElements.size() > 1 && el.getAttributeValue("rank").equalsIgnoreCase("family"))
				continue;
			else {
				if(el.getAttributeValue("rank").equalsIgnoreCase("section"))
					continue;
				if(el.getAttributeValue("rank").equalsIgnoreCase("subsection"))
					continue;
				if(el.getAttributeValue("rank").equalsIgnoreCase("subgenus"))
					continue;
				if(el.getAttributeValue("rank").equalsIgnoreCase("subfamily"))
					continue;
				if(el.getAttributeValue("rank").equalsIgnoreCase("tribe"))
					continue;
				if(el.getAttributeValue("rank").equalsIgnoreCase("subspecies"))
					sb.append("subsp. ");
				if(el.getAttributeValue("rank").equalsIgnoreCase("series"))
					sb.append("ser. ");
				if(el.getAttributeValue("rank").equalsIgnoreCase("variety"))
					sb.append("var. ");
				sb.append(el.getValue()+ " ");
			}
		}
		return sb.toString().replaceAll("\\s+", " ").trim();
	}

}
