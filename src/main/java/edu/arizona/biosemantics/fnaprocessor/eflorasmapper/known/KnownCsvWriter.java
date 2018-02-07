package edu.arizona.biosemantics.fnaprocessor.
eflorasmapper.known;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.arizona.biosemantics.common.taxonomy.Rank;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlState;
import edu.arizona.biosemantics.fnaprocessor.eflorascrawler.CrawlStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.DefaultMapStateReporter;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapState;
import edu.arizona.biosemantics.fnaprocessor.taxonname.FileNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.combinatorics.AcceptedNameExtractor;
import edu.arizona.biosemantics.fnaprocessor.taxonname.combinatorics.AnyNameExtractor;
import au.com.bytecode.opencsv.CSVWriter;

public class KnownCsvWriter {
	
	static Comparator<Element> rankComparator = new Comparator<Element>() {
		@Override
		public int compare(Element o1, Element o2) {
			return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() - 
					Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
		}	
	};
	
	private static Logger logger = Logger.getLogger(DefaultMapStateReporter.class);
	private AcceptedNameExtractor acceptedNameExtractor;
	private AnyNameExtractor anyNameExtractor;
	private FileNameExtractor fileNameExtractor;
	private CrawlStateProvider crawlStateProvider;
	private Map<String, File> volumeUrlDirMap;
	private Map<String, String> volumeUrlNameMap;
	
	@Inject
	public KnownCsvWriter(AcceptedNameExtractor acceptedNameExtractor, AnyNameExtractor anyNameExtractor, 
			FileNameExtractor fileNameExtractor, CrawlStateProvider crawlStateProvider,
			@Named("volumeUrlDirMap") Map<String, File> volumeUrlDirMap,
			@Named("volumeUrlNameMap")Map<String, String> volumeUrlNameMap) {
		this.acceptedNameExtractor = acceptedNameExtractor;
		this.anyNameExtractor = anyNameExtractor;
		this.fileNameExtractor = fileNameExtractor;
		this.crawlStateProvider = crawlStateProvider;
		this.volumeUrlDirMap = volumeUrlDirMap;
		this.volumeUrlNameMap = volumeUrlNameMap;
	}
	
	public void write(MapState mapState) throws Exception {
		CrawlState crawlState = this.crawlStateProvider.getCrawlState(mapState.getVolumeUrl());
		try(CSVWriter writer = new CSVWriter(new FileWriter(new File("known-" + volumeUrlNameMap.get(mapState.getVolumeUrl()) + ".csv")))) {
			
			List<String[]> lines = new ArrayList<String[]>();
			
			for(File file : mapState.getMappedFiles()) {
				String url = mapState.getUrl(file);
				Class<?> mapper = mapState.getMapper(file);
				String linkName = crawlState.getLinkName(url);
				lines.add(new String[] { "(" + extractName(file) + ") " + file.getName(), "(" + linkName + ") " + url, mapper.getName() });
			}
			
			for(File file : getUnmappedFiles(mapState)) {
				lines.add(new String[] { "(" + extractName(file) + ") " + file.getName(), ""});
			}
			
			List<String> unmappedUrls = new ArrayList<String>();
			for(String url : crawlState.getUrls()) {
				String name = crawlState.getLinkName(url);
				//a single (not-yet mapped) taxon treatment page
				if(name != null) {
					if(!mapState.hasFile(url))
						unmappedUrls.add(url);
				}
			}
			for(String url : unmappedUrls) {
				String linkName = crawlState.getLinkName(url);
				lines.add(new String[] { "", "(" + linkName + ") " + url });
			}
			
			writer.writeAll(lines);
		}
	}
	
	private List<File> getUnmappedFiles(MapState mapState) {
		List<File> result = new ArrayList<File>();
		for(File file : this.volumeUrlDirMap.get(mapState.getVolumeUrl()).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		})) {
			if(!mapState.getFileToDocumentMaping().containsKey(file))
				result.add(file);
		}
		return result;
	}
	
	private static String extractName(File f) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = (Document) builder.build(f);
		
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> acceptedNameExpression =
				xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name", Filters.element());
		
		List<Element> acceptedNameElements = new ArrayList<Element>(acceptedNameExpression.evaluate(document));
		acceptedNameElements.sort(rankComparator);
		
		StringBuffer sb = new StringBuffer();
		for(Element el : acceptedNameElements) {
			sb.append(el.getValue() + " ");
		}
		
		//AcceptedNameExtractor acceptedNameExtractor = new AcceptedNameExtractor();
		//Set<String> set = acceptedNameExtractor.extract(f);
		//return value.trim().replaceAll("[^a-zA-Z_0-9.<>\\s]", "").replaceAll("\\s+", " ").toLowerCase();
		return sb.toString().replaceAll("\\s+", " ").trim();
	}
	
}
