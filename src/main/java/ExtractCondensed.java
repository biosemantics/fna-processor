import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class ExtractCondensed {

	
	public static void main(String[] args) {
		try {
			File file = new File("combined_all_map.log");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if(line.contains("***") || line.contains("StackVolumeMapper") || line.contains("SerializedCrawlStateProvider")) {
					stringBuffer.append(line);
					stringBuffer.append("\n");
				}
			}
			fileReader.close();
			System.out.println("Contents of file:");
			System.out.println(stringBuffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
