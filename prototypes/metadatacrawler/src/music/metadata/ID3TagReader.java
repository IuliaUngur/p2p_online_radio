package music.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import utils.FileUtils;
import utils.MessageHandler;
import utils.MessageHandler.LogType;

public class ID3TagReader {
	
	private final String[] ALLOWED_FILE_EXTENSION = {".mp3"};
	public ID3TagReader() {
		
	}
	
	public HashMap<String, MusicMetaData> getMetaDataCatalog(String directory) throws FileNotFoundException {
		HashMap<String, MusicMetaData> catalog = new HashMap<String, MusicMetaData>();
		ArrayList<String> files = FileUtils.listDir(new File(directory));
		ArrayList<String> allowedFiles = new ArrayList<String>();
		for(String f : files) {
			if(hasAllowedFileExtension(f)) {
				allowedFiles.add(f);
			}
		}
		files.clear();
		
		for(String allowedFile : allowedFiles) {
			MusicMetaData metaData = getMetaData(allowedFile);
			catalog.put(allowedFile, metaData);
		}
		
		return catalog;
	}
	
	
	private boolean hasAllowedFileExtension(String filename) {
		for(String ext : ALLOWED_FILE_EXTENSION) {
			if(filename.toLowerCase().endsWith(ext.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	public MusicMetaData getMetaData(String filePath) throws FileNotFoundException {
		if (filePath == null) {
		      throw new IllegalArgumentException("Argument filePath should not be null.");
	    }
		File file = new File(filePath);
		return getMetaData(file);
	}
	
	public MusicMetaData getMetaData(File file) throws FileNotFoundException {
		if (file == null) {
		      throw new IllegalArgumentException("Argument file should not be null.");
	    }
		if (!file.exists()) {
			throw new FileNotFoundException ("File does not exist: " + file.getName());
	    }
	    if (!file.isFile()) {
	      throw new IllegalArgumentException("Is not a file: " + file.getName());
	    }
	    if (!file.canRead()) {
	      throw new IllegalArgumentException("File cannot be read: " + file.getName());
	    }
	    if(!hasAllowedFileExtension(file.getName())) {
	    	StringBuilder sb = new StringBuilder();
	    	for(String ext : ALLOWED_FILE_EXTENSION) {
	    		sb.append(ext).append(" ");
	    	}
	    	throw new IllegalArgumentException("File does not have allowed extension. Allowed extensions: " + sb.toString());
	    }
	    
	    MusicMetaData metaData = new MusicMetaData();;
	    MP3File musicFile;
		try {
			musicFile = (MP3File)AudioFileIO.read(file);
			metaData.setFileName(file.getName());
			metaData.setFileSize(file.length());
			metaData.setTrackLength(musicFile.getAudioHeader().getTrackLength());
			if(musicFile.hasID3v2Tag()) {
				ID3v24Tag tag = musicFile.getID3v2TagAsv24();
				if(tag.hasField(FieldKey.ARTIST)) metaData.setArtist(tag.getFirst(FieldKey.ARTIST));
				if(tag.hasField(FieldKey.ALBUM)) metaData.setAlbum(tag.getFirst(FieldKey.ALBUM));
				if(tag.hasField(FieldKey.TITLE)) metaData.setTitle(tag.getFirst(FieldKey.TITLE));
				if(tag.hasField(FieldKey.GENRE)) metaData.setGenre(tag.getFirst(FieldKey.GENRE));
			}
			
			if(musicFile.hasID3v1Tag()) {
				ID3v1Tag tag = musicFile.getID3v1Tag();
				if(!metaData.hasArtist() && tag.hasField(FieldKey.ARTIST)) metaData.setArtist(tag.getFirst(FieldKey.ARTIST));
				if(!metaData.hasAlbum() && tag.hasField(FieldKey.ALBUM)) metaData.setAlbum(tag.getFirst(FieldKey.ALBUM));
				if(!metaData.hasTitle() && tag.hasField(FieldKey.TITLE)) metaData.setTitle(tag.getFirst(FieldKey.TITLE));
				if(!metaData.hasGenre() && tag.hasField(FieldKey.GENRE)) metaData.setGenre(tag.getFirst(FieldKey.GENRE));
			}
		   
		} catch (Exception ex) {
			ex.printStackTrace();
			MessageHandler.log(LogType.debug, ex.getMessage());
		} 
		
		return metaData;
	}
}
