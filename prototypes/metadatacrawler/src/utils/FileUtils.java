package utils;

import java.io.File;
import java.util.ArrayList;

public class FileUtils {
	
	public static ArrayList<String> listDir(File dir) {	
		if(dir == null) {
			throw new IllegalArgumentException("Parameter dir should not be null.");
		}
		
		ArrayList<String> allFiles = new ArrayList<String>();
		if(dir.isFile()) {
			allFiles.add(dir.getAbsolutePath());
			return allFiles;
		}
		
		File[] files = dir.listFiles(); // initial file list in current directory
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					ArrayList<String> tmpFiles = listDir(files[i]); // recursion
					allFiles.addAll(tmpFiles);
				} else {
					allFiles.add(files[i].getAbsolutePath());
				}
			}
		}
		return allFiles;
	}	
}
