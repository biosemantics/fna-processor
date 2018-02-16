package edu.arizona.biosemantics.fnaprocessor.taxonname.combinatorics;

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
import edu.arizona.biosemantics.fnaprocessor.taxonname.Normalizer;

/**
 * Extracts name candidates by using a combinatorics approach and considering accepted name elements only
 */
public class AcceptedNameExtractor extends AbstractNameExtractor {

	@Override
	protected LinkedHashMap<String, Set<String>> createRankNameOptions(Document document) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> acceptedNameExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element());

		List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
		Comparator<Element> rankComparator = new Comparator<Element>() {
			@Override
			public int compare(Element o1, Element o2) {
				return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() -
						Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
			}
		};
		acceptedNameElements.sort(rankComparator);
		LinkedHashMap<String, Set<String>> rankNameOptions = new LinkedHashMap<String, Set<String>>();
		for(Element acceptedNameElement : acceptedNameElements) {
			String rank = Normalizer.normalize(acceptedNameElement.getAttributeValue("rank"));
			rankNameOptions.put(rank, new HashSet<String>(Arrays.asList(Normalizer.normalize(acceptedNameElement.getValue()))));
		}
		return rankNameOptions;
	}

}
