import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import edu.arizona.biosemantics.common.taxonomy.Rank;
import edu.arizona.biosemantics.fnaprocessor.Configuration;
import edu.arizona.biosemantics.fnaprocessor.taxonname.conventional.AcceptedNameExtractor;


public class EnhanceLog {
	
	static Comparator<Element> rankComparator = new Comparator<Element>() {
		@Override
		public int compare(Element o1, Element o2) {
			return Rank.valueOf(o1.getAttribute("rank").getValue().trim().toUpperCase()).getId() - 
					Rank.valueOf(o2.getAttribute("rank").getValue().trim().toUpperCase()).getId();
		}	
	};

	public static void main(String[] args) throws JDOMException, IOException {
		try {
			File file = new File("combined_all_map.log");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			
			String volume = "";
			while ((line = bufferedReader.readLine()) != null) {
				if(line.contains("SerializedCrawlStateProvider:32 - Deserialized crawlState for volume")) {
					//2018-01-15 22:33:25 INFO  SerializedCrawlStateProvider:32 - Deserialized crawlState for volume v2
					volume = line.split("SerializedCrawlStateProvider:32 - Deserialized crawlState for volume ")[1];
				}
				
					
				if(line.contains("DefaultMapStateReporter:71")) {
					//2018-01-15 22:33:25 INFO  DefaultMapStateReporter:71 - Abies fraseri.xml
					String[] parts = line.split(" - ");
					String name = extractName(volume, parts[1]);
					line = parts[0] + " - " + name + " - " + parts[1];
					stringBuffer.append(line + "\n");
				} else {
					stringBuffer.append(line + "\n");
				}
			}
			fileReader.close();
			
			FileWriter fileWriter = new FileWriter(new File("combined_all_map_enhance.log"));
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.write(stringBuffer.toString());
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//extractName("V3", "Alnus_incana_subsp._rugosa.xml");
	}

	private static String extractName(String volume, String file) throws JDOMException, IOException {
		if(volume.equals("v19"))
			volume = "V19-20-21";
		File f = new File(Configuration.fnaTextProcessingDirectory + File.separator + volume + File.separator + file);
		
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
		return sb.toString().replaceAll("\\s+", " ");
	}

}
