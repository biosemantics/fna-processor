import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.arizona.biosemantics.fnaprocessor.Configuration;


public class UpdateKnownFilesToChangedRepoStructure {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Map<String, String> volumeUrlNameMap = new LinkedHashMap<String, String>();
		Map<File, String> volumeDirUrlMap = new LinkedHashMap<File, String>();
		Map<String, File> volumeUrlDirMap = new LinkedHashMap<String, File>();

		int[] volumes = new int[] {
				2,3,4,5,6,8,9,
				7,
				19,
				22,23,
				26,27,28
		};
		for(int volume : volumes) {
			String volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=10" + String.format("%02d", volume) + "&flora_id=1";
			File volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
			switch(volume) {
			case 2:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
				break;
			case 3:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
				break;
			case 19:
				//volume 19 is for 19-20-21 volumes since they are managed under one and the same url on efloras
				if(volume == 19) {
					volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=1019&flora_id=1";
					volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V19-20-21");
				}
				break;
			case 22:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
			}

			volumeUrlNameMap.put(volumeUrl, "v" + volume);
			volumeDirUrlMap.put(volumeDir, volumeUrl);
			volumeUrlDirMap.put(volumeUrl, volumeDir);
		}

		File volumesDir = new File(Configuration.fnaTextProcessingDirectory);


		for(File volumeDir : volumesDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() && !file.getName().startsWith(".");
			}
		})) {
			String volumeUrl = volumeDirUrlMap.get(volumeDir);
			String volumeName = volumeUrlNameMap.get(volumeUrl);

			File known = new File("updated_known-" + volumeName + "-Feb22.csv");
			if(known.exists() && known.isFile()) {
				try(CSVReader reader = new CSVReader(new FileReader(known))) {
					List<String[]> lines = reader.readAll();
					for(String[] line : lines) {
						//if(line[0].trim().isEmpty() || line[1].trim().isEmpty())
						//	continue;

						if(line[0].contains(")")) {
							line[0] = line[0].split("\\)")[1].trim();
						}
						if(line[1].contains(")")) {
							line[1] = line[1].split("\\)")[1].trim();
						}

						if(!line[0].startsWith("V")) {
							if(line[0].startsWith("v")) {
								line[0] = line[0].replaceFirst("v", "V");
							} else {
								line[0] = volumeName.toUpperCase() + "_" + line[0];
							}
						}
					}

					try(CSVWriter writer = new CSVWriter(new FileWriter(known))) {
						writer.writeAll(lines);
					}
				}
			}
		}
	}

}
