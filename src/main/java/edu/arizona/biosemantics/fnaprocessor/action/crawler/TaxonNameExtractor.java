package edu.arizona.biosemantics.fnaprocessor.action.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import edu.arizona.biosemantics.common.taxonomy.Rank;


/**
 * leave out ranks, all possibilities
 * use rank abbreviations, all possibilities
 * use synonyms instead of accepted names, all possibilties 
 */
public class TaxonNameExtractor {
	
	private static Logger logger = Logger.getLogger(TaxonNameExtractor.class);
	private Map<File, Set<String>> nameExtractionCache = new HashMap<File, Set<String>>();

	public static void main(String[] args) throws JDOMException, IOException {
		TaxonNameExtractor extr = new TaxonNameExtractor();
		logger.info(extr.extract(new File("C:\\Users\\updates\\git\\FNATextProcessing\\V6\\79.xml")));
	}
	
	public Set<String> extract(File file) throws JDOMException, IOException {
		if(nameExtractionCache.containsKey(file))
			return nameExtractionCache.get(file);
		Set<String> result = new HashSet<String>();
		SAXBuilder builder = new SAXBuilder();
		Document document = (Document) builder.build(file);
		
		LinkedHashMap<String, Set<String>> rankNameOptions = createRankNameOptions(document);
		
		for(int n = 0; n < Math.pow(2, rankNameOptions.size()); n++) {
			String enabledNameParameters = String.format("%" + rankNameOptions.size() + "s", Integer.toBinaryString(n)).replace(' ', '0');
			
			LinkedHashMap<String, Boolean> enabledNames = new LinkedHashMap<String, Boolean>();
			String[] ranks = new String[rankNameOptions.size()];
			ranks = rankNameOptions.keySet().toArray(ranks);
			for(int i = 0; i < rankNameOptions.size(); i++) {
				String rank = ranks[i];
				enabledNames.put(rank, enabledNameParameters.charAt(i) == '1');
			}
			
			for(int p = 0; p < Math.pow(2, rankNameOptions.size()); p++) {
				String enabledAbbreviationParameters = String.format("%" + rankNameOptions.size() + "s", Integer.toBinaryString(p)).replace(' ', '0');
				
				LinkedHashMap<String, Boolean> enabledAbbreviations = new LinkedHashMap<String, Boolean>();
				for(int i = 0; i < rankNameOptions.size(); i++) {
					String rank = ranks[i];
					enabledAbbreviations.put(rank, enabledAbbreviationParameters.charAt(i) == '1');
				}
				result.addAll(this.getNameVariants(rankNameOptions, 
						enabledNames, enabledAbbreviations));
			}
		}
		result.add(normalizeTaxonName(file.getName().replace(".xml", "")));
		
		nameExtractionCache.put(file, result);
		return result;
	}
	
	private List<String> getNameVariants(LinkedHashMap<String, Set<String>> rankNameOptions,
			LinkedHashMap<String, Boolean> enabledNames, LinkedHashMap<String, Boolean> enabledAbbreviations) {
		StringBuilder templateSb = new StringBuilder();
		for (String rank : rankNameOptions.keySet()) {
			if(enabledNames.get(rank)) {
				if(enabledAbbreviations.get(rank)) {
					templateSb.append(getRankAbbreviation(rank) + " ");
				}
				templateSb.append("<" + rank + "> ");
			}			
		}

		String template = templateSb.toString();
		List<String> nameSkeletons = new ArrayList<String>(Arrays.asList(template));
		for(String rank : rankNameOptions.keySet()) {
			List<String> newNameSkeletons = new ArrayList<String>();
			for(String nameSkeleton : nameSkeletons) {
				newNameSkeletons.addAll(getFilledNameSkeleton(nameSkeleton, rank, rankNameOptions.get(rank)));
			}
			nameSkeletons = newNameSkeletons;
		}
		
		List<String> result = new ArrayList<String>();
		for(String name : nameSkeletons) {
			name = normalizeTaxonName(name);
			if(!name.isEmpty())
				result.add(name);
		}
		return result;
	}

	private List<String> getFilledNameSkeleton(String skeleton, String rank, Set<String> nameOptions) {
		List<String> nameSkeletons = new ArrayList<String>(nameOptions.size());
		for(String nameOption : nameOptions) {
			String name = skeleton.replaceAll("<" + rank + ">", nameOption);
			nameSkeletons.add(name);
		}
		return nameSkeletons;
	}

	private String getRankAbbreviation(String rank) {
		switch(rank) {
		case "subspecies":
			return "subsp.";
		case "variety":
			return "var.";
		case "subfamily":
			return "subfam.";
		case "subtribe":
			return "subtribe";
		case "subgenus":
			return "subg.";
		case "tribe":
			return "tribe";
		case "section":
			return "sect.";
		case "subsection":
			return "subsect.";
		case "series":
			return "ser.";
		}
		return "";
	}

	private LinkedHashMap<String, Set<String>> createRankNameOptions(Document document) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> acceptedNameExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element());
		XPathExpression<Element> synonymNameExpression = 
				xFactory.compile("//taxon_identification[@status='SYNONYM']/taxon_name | "
						+ "//taxon_identification[@status='BASONYM']/taxon_name", Filters.element());
		
		List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
		List<Element> synonymNameElements = new ArrayList<Element>(synonymNameExpression.evaluate(document));
		Comparator<Element> rankComparator = new Comparator<Element>() {
			@Override
			public int compare(Element o1, Element o2) {
				return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() - 
						Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
			}	
		};
		acceptedNameElements.sort(rankComparator);
		synonymNameElements.sort(rankComparator);
		LinkedHashMap<String, Set<String>> rankNameOptions = new LinkedHashMap<String, Set<String>>();
		for(Element acceptedNameElement : acceptedNameElements) {
			String rank = normalizeTaxonName(acceptedNameElement.getAttributeValue("rank"));
			rankNameOptions.put(rank, new HashSet<String>(Arrays.asList(normalizeTaxonName(acceptedNameElement.getValue()))));
		}
		for(Element synonymNameElement : synonymNameElements) {
			String rank = normalizeTaxonName(synonymNameElement.getAttributeValue("rank"));
			if(!rankNameOptions.containsKey(rank))
				rankNameOptions.put(rank, new HashSet<String>());
			rankNameOptions.get(rank).add(normalizeTaxonName(synonymNameElement.getValue()));
		}
		return rankNameOptions;
	}
	
	private String normalizeTaxonName(String value) {
		return value.trim().replaceAll("[^a-zA-Z_0-9.<>\\s]", "").replaceAll("\\s+", " ").toLowerCase();
	}
}
