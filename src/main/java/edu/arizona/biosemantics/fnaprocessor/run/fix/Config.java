package edu.arizona.biosemantics.fnaprocessor.run.fix;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.fnaprocessor.Configuration;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.MapStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.SerializedMapStateProvider;
import edu.arizona.biosemantics.fnaprocessor.eflorasmapper.known.KnownVolumeMapper;
import edu.arizona.biosemantics.fnaprocessor.run.BaseConfig;
import edu.arizona.biosemantics.fnaprocessor.run.Run;

public class Config extends BaseConfig {

	@Override
	protected void configure() {
		super.configure();
		
		Map<String, String> volumeUrlNameMap = new LinkedHashMap<String, String>();
		Map<File, String> volumeDirUrlMap = new LinkedHashMap<File, String>();
		Map<String, File> volumeUrlDirMap = new LinkedHashMap<String, File>();
		
		int[] volumes = new int[] {
			6
				//24
			/*2,3,4,5,6,8,9,
			7,
			19,
			22,23,
			24, 25,
			26,27,28*/
		};
		for(int volume : volumes) {
			String volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=10" + String.format("%02d", volume) + "&flora_id=1";
			File volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume);
			switch(volume) {
			case 2:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
				break;
			case 3:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
				break;
			case 19:
				//volume 19 is for 19-20-21 volumes since they are managed under one and the same url on efloras
				if(volume == 19) {
					volumeUrl = "http://www.efloras.org/volume_page.aspx?volume_id=1019&flora_id=1";
					volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V19-20-21");
				}
				break;
			case 22:
				volumeDir = new File(Configuration.fnaTextProcessingDirectory + File.separator + "V" + volume + File.separator + "numerical_files");
			}
			
			volumeUrlNameMap.put(volumeUrl, "v" + volume);
			volumeDirUrlMap.put(volumeDir, volumeUrl);
			volumeUrlDirMap.put(volumeUrl, volumeDir);
		}

		
		bind(new TypeLiteral<Map<String, String>>() {}).annotatedWith(Names.named("volumeUrlNameMap"))
			.toInstance(volumeUrlNameMap);
		bind(new TypeLiteral<Map<File, String>>() {}).annotatedWith(Names.named("volumeDirUrlMap"))
			.toInstance(volumeDirUrlMap);
		bind(new TypeLiteral<Map<String, File>>() {}).annotatedWith(Names.named("volumeUrlDirMap"))
		.toInstance(volumeUrlDirMap);
		
				
		bind(File.class).annotatedWith(Names.named("volumesDir")).toInstance(new File(Configuration.fnaTextProcessingDirectory));
		bind(Run.class).to(FixFnaVolumesRun.class);
		
		bind(MapStateProvider.class).annotatedWith(Names.named("serializedMapStateProvider")).to(KnownVolumeMapper.class);

	}
	

}
