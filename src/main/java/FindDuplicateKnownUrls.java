import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;



public class FindDuplicateKnownUrls {

	public static void main(String[] args) throws Exception {
		int[] volumes = new int[] {
				2,3,4,5,6,8,9,
				7,
				19,
				22,23,
				26,27,28
		};
		for(int volume : volumes) {
			System.out.println("Volume " + volume);
			Set<String> urls = new HashSet<String>();
			try(CSVReader reader = new CSVReader(new FileReader(new File("updated_known-v" + volume + "-Feb22.csv")))) {
				List<String[]> lines = reader.readAll();
				for(String[] line : lines) {
					if(line[0].trim().isEmpty() || line[1].trim().isEmpty())
						continue;

					if(line[0].contains(")")) {
						line[0] = line[0].split("\\)")[1].trim();
					}
					if(line[1].contains(")")) {
						line[1] = line[1].split("\\)")[1].trim();
					}
					if(!urls.contains(line[1])) {
						urls.add(line[1]);
					} else {
						System.out.println("Duplicate url: " + line[1]);
					}

				}
			}
		}
	}
}
