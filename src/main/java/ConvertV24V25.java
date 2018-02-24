import java.io.File;

import edu.arizona.biosemantics.fnaprocessor.action.schemacheck.SchemaCheckAction;
import edu.arizona.biosemantics.fnaprocessor.action.schemaconvert.ConvertOldSchemaAction;


public class ConvertV24V25 {

	public static void main(String[] args) throws Exception {
		//enhance crawlstate with unseen urls in mapstate // nevermind we do not have a mapping for these two volumes

		//convert mapstate with updated file paths // nevermind no mapstate for these

		File volumeDir = new File("C:/Users/rodenhausen.CATNET/git/jocelyn_files/coarse_grained_fna_xml/V25");

		ConvertOldSchemaAction convertOldSchemaAction = new ConvertOldSchemaAction();
		convertOldSchemaAction.run(volumeDir);


		/*Map<File, String> volumeDirUrlMap = new HashMap<>();
		String volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=10" + String.format("%02d", 24) + "&flora_id=1";
		volumeDirUrlMap.put(volumeDir, volumeUrl);
		Map<String, File> volumeUrlDirMap = new HashMap<>();
		volumeUrlDirMap.put(volumeUrl, volumeDir);
		Map<String, String> volumeUrlNameMap = new HashMap<String, String>();
		volumeUrlNameMap.put(volumeUrl, "V24");
		File crawlStateDir = new File("crawlState");

		CrawlStateProvider crawlStateProvider = new SerializedCrawlStateProvider(crawlStateDir, volumeUrlNameMap);
		HrefResolver hrefResolver = new HrefResolver();
		KnownCsvReader knownCsvReader = new KnownCsvReader(volumeUrlDirMap, volumeUrlNameMap);
		MapStateProvider mapStateProvider = new KnownVolumeMapper(knownCsvReader);
		KeyAction keyAction = new KeyAction(crawlStateProvider, hrefResolver, mapStateProvider, volumeDirUrlMap);
		keyAction.run(volumeDir);*/


		SchemaCheckAction schemaCheckAction = new SchemaCheckAction();
		schemaCheckAction.run(volumeDir);
	}

}
