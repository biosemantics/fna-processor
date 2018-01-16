package edu.arizona.biosemantics.fnaprocessor.taxonname.conventionalcrossnameclass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import edu.arizona.biosemantics.common.taxonomy.Rank;

public class AnyNameExtractor extends AbstractNameExtractor {

	protected LinkedHashMap<String, Set<String>> createRankNameOptions(Document document) {
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
}
