package edu.arizona.biosemantics.fnaprocessor.taxonname.conventional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import edu.arizona.biosemantics.fnaprocessor.taxonname.Normalizer;

public class AnyNameExtractor extends AbstractNameExtractor {
	
	protected List<LinkedHashMap<String, Set<String>>> createNameOptions(Document document) {
		List<LinkedHashMap<String, Set<String>>> list = new ArrayList<>();
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> acceptedNameExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']", Filters.element());
		XPathExpression<Element> synonymNameExpression = 
				xFactory.compile("//taxon_identification[@status='SYNONYM']", Filters.element());
		XPathExpression<Element> basionymNameExpression = 
				xFactory.compile("//taxon_identification[@status='BASONYM']", Filters.element());
		
		List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
		List<Element> synonymNameElements = new ArrayList<Element>(synonymNameExpression.evaluate(document));
		List<Element> basionymNameElements = new ArrayList<Element>(basionymNameExpression.evaluate(document));
		
		this.addNameOptions(acceptedNameElements, list);
		this.addNameOptions(synonymNameElements, list);
		this.addNameOptions(basionymNameElements, list);
		return list;
	}

	private void addNameOptions(List<Element> nameElements, List<LinkedHashMap<String, Set<String>>> result) {
		for(Element nameElement : nameElements) {
			List<Element> rankElements = new ArrayList<Element>(nameElement.getChildren("taxon_name"));
			rankElements.sort(rankComparator);
			
			LinkedHashMap<String, Set<String>> rankNameOptions = new LinkedHashMap<String, Set<String>>();
			for(Element rankElement : rankElements) {
				String rank = Normalizer.normalize(rankElement.getAttributeValue("rank"));
				rankNameOptions.put(rank, new HashSet<String>(Arrays.asList(Normalizer.normalize(rankElement.getValue()))));
			}
			result.add(rankNameOptions);
		}
	}
}
