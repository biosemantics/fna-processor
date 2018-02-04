package edu.arizona.biosemantics.fnaprocessor.taxonname.conventional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.jdom2.input.SAXBuilder;

import edu.arizona.biosemantics.common.taxonomy.Rank;
import edu.arizona.biosemantics.fnaprocessor.taxonname.Normalizer;
import edu.arizona.biosemantics.fnaprocessor.taxonname.TaxonNameExtractor;

/**
 * Only allows names of consecutive ranks (i.e. no omission of in-between ranks)
 * Only allows names ending with the lowest level rank
 */
public abstract class AbstractNameExtractor implements TaxonNameExtractor {

	private static Logger logger = Logger.getLogger(AbstractNameExtractor.class);
	private Map<File, Set<String>> nameExtractionCache = new HashMap<File, Set<String>>();

	protected Comparator<Element> rankComparator = new Comparator<Element>() {
		@Override
		public int compare(Element o1, Element o2) {
			return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() - 
					Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
		}	
	};
	
	public static void main(String[] args) throws JDOMException, IOException {
		AcceptedNameExtractor extr = new AcceptedNameExtractor();
		
		//logger.info(extr.extract(new File("C:\\Users\\updates\\git\\FNATextProcessing\\V2\\Abies amabilis.xml")).size());
		logger.info(extr.extract(new File("C:\\Users\\rodenhausen.CATNET\\git2018\\FNATextProcessing\\V5\\99.xml")));
	}
	
	@Override
	public Set<String> extract(File file) throws JDOMException, IOException {
		if(nameExtractionCache.containsKey(file))
			return nameExtractionCache.get(file);
		Set<String> result = new HashSet<String>();
		SAXBuilder builder = new SAXBuilder();
		Document document = (Document) builder.build(file);
		
		

		List<LinkedHashMap<String, Set<String>>> nameOptions = createNameOptions(document);
		for(LinkedHashMap<String, Set<String>> rankNameOptions : nameOptions) {
			for(int nameLength = 1; nameLength <= rankNameOptions.size(); nameLength++) {
				String[] ranks = new String[rankNameOptions.size()];
				ranks = rankNameOptions.keySet().toArray(ranks);
				
				Map<String, Boolean> enabledNames = new HashMap<String, Boolean>();
				for(int i = 0; i < rankNameOptions.size(); i++) {
					String rank = ranks[i];
					enabledNames.put(rank, i >= ranks.length - nameLength);
				}
				
				//enable or disable rank abbreviations all possible combinations
				double maxP = Math.pow(2, nameLength);
				for(int p = 0; p < maxP; p++) {
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
			
			/**
			 * Make sure to include the binomial name in either case
			 */
			result.addAll(this.getConsecutiveNameForRanks(rankNameOptions, "genus", "species"));
			
			/**
			 * Make sure to include the binomial + variety name in either case
			 */
			result.addAll(this.getConsecutiveNameForRanks(rankNameOptions, "genus", "species", "variety"));
			
			/**
			 * Make sure to include binomial + subspecies name in either case
			 */
			result.addAll(this.getConsecutiveNameForRanks(rankNameOptions, "genus", "species", "subspecies"));
		}
		
		nameExtractionCache.put(file, result);
		return result;
	}

	private Collection<? extends String> getConsecutiveNameForRanks(LinkedHashMap<String, Set<String>> rankNameOptions,
			String... ranks) {
		LinkedHashMap<String, Set<String>> rankNameOptionsBinomial = new LinkedHashMap<String, Set<String>>();
		LinkedHashMap<String, Boolean> enabledAbbreviations = new LinkedHashMap<String, Boolean>();
		Map<String, Boolean> enabledNames = new LinkedHashMap<String, Boolean>();
		
		for(String rank : ranks) {
			if(!rankNameOptions.containsKey(rank))
				return new HashSet<String>();
			rankNameOptionsBinomial.put(rank, rankNameOptions.get(rank));
			enabledAbbreviations.put(rank, false);	
			enabledNames.put(rank, true);
		}
		return this.getNameVariants(rankNameOptionsBinomial, enabledNames, enabledAbbreviations);
	}

	private List<String> getNameVariants(LinkedHashMap<String, Set<String>> rankNameOptions,
			Map<String, Boolean> enabledNames, Map<String, Boolean> enabledAbbreviations) {
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
			name = Normalizer.normalize(name);
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

	protected abstract List<LinkedHashMap<String, Set<String>>> createNameOptions(Document document);
	
}
