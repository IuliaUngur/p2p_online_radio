package music.metadatacrawler;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Starter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String command = "";
		do {
			if(command.trim().equalsIgnoreCase("")) {
				System.out.println("Usage and Commands: \n-f file\n-d directory\n-q quit");		
				continue;
			}
			String[] arguments = command.trim().split(" ", 2);
			try { 
				if(arguments.length %2 != 0)
				{
					System.err.println("some parameters missing? odd number of arguments.");
				} else {
					for(int index = 0; index < arguments.length; index += 2) {
						String key = arguments[index]; 	String value = arguments[index+1];
		
						if(key.equals("-d"))
						{
							processDirectory(value);
						} else if (key.equals("-f")) {
							processFile(value);
						}
					}
					
				}
			} catch (Exception ex) {
				System.err.println("Exception occured in metadata crawler module: " + ex.getMessage());
				ex.printStackTrace();
			} 
		} while ((command = scanner.nextLine()) != null && !command.equalsIgnoreCase("-q"));
		
		System.out.println("Quit");
			
	}

	private static void processFile(String filePath) throws FileNotFoundException {
		System.out.println("processing file " + filePath);
		ID3TagReader tagReader = new ID3TagReader();
		MusicMetaData metaData = tagReader.getMetaData(filePath);
		System.out.println(metaData.toString());
	}

	private static void processDirectory(String path) throws FileNotFoundException {
		System.out.println("processing folder " + path);
		ID3TagReader tagReader = new ID3TagReader();
		HashMap<String, MusicMetaData> catalog = tagReader.getMetaDataCatalog(path);
		System.out.println("Total files in catalog: " + catalog.size());
		for(String key : catalog.keySet()) {
			System.out.println(catalog.get(key).toString());
		}
	}

}
