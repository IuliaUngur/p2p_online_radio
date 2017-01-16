package core;

import java.io.FileNotFoundException;
import java.util.HashMap;

import music.metadata.ID3TagReader;
import music.metadata.MusicMetaData;

import utils.MessageHandler;
import utils.MessageHandler.LogType;

public class Starter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try { 
			if(args.length == 0)
			{
				MessageHandler.log(LogType.info, "Usage and Commands: \n-f file\n-d directory");
			} else if (args.length % 2 != 0) {
				MessageHandler.log(LogType.warning, "some parameters missing? odd number of arguments.");
			} else {
				for(int index = 0; index < args.length; index += 2) {
					String key = args[index]; 	String value = args[index+1];
	
					if(key.equals("-d"))
					{
						processDirectory(value);
					} else if (key.equals("-f")) {
						processFile(value);
					}
				}
				
			}
		} catch (Exception ex) {
			MessageHandler.log(LogType.warning, ex.getMessage());
		} 
			
	}

	private static void processFile(String filePath) throws FileNotFoundException {
		MessageHandler.log(LogType.debug, "processing file " + filePath);
		ID3TagReader tagReader = new ID3TagReader();
		MusicMetaData metaData = tagReader.getMetaData(filePath);
		System.out.println(metaData.toString());
	}

	private static void processDirectory(String path) throws FileNotFoundException {
		MessageHandler.log(LogType.debug, "processing folder " + path);
		ID3TagReader tagReader = new ID3TagReader();
		HashMap<String, MusicMetaData> catalog = tagReader.getMetaDataCatalog(path);
		System.out.println("Total files in catalog: " + catalog.size());
		for(String key : catalog.keySet()) {
			System.out.println(catalog.get(key).toString());
		}
	}

}
