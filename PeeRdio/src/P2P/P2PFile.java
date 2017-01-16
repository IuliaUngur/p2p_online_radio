package P2P;

import java.io.IOException;
import java.io.Serializable;

import utils.FileUtils;

import core.MusicFile;


public class P2PFile implements Serializable{

	private static final long serialVersionUID = 1L;
	private byte[] data;
	private String filename;
	
	public byte[] getData() {
		return data;
	}
	
	public String getFilename() {
		return filename;
	}
	
	
	public P2PFile(String filename, byte[] data) {
		this.filename = filename; 
		this.data = data;
	}
	
	public static P2PFile createP2pFile(MusicFile musicFile) throws IOException, Exception {
		if(musicFile == null) return null;
		return new P2PFile(musicFile.getFile().getName(), FileUtils.loadFileIntoByteArray(musicFile.getFile()));
	}

}
