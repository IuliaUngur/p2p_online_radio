package core;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import nanoxml.XMLElement;
import net.tomp2p.peers.Number160;
import utils.FileUtils;

import P2P.interfaces.IP2PKeywordElement;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import utils.Constants;
import music.metadatacrawler.ID3TagReader;
import music.metadatacrawler.MusicMetaData;

public class MusicFile implements IP2PKeywordElement {

	private static final long serialVersionUID = 1L;
	private transient File file;
	private MusicMetaData metadata;
	private Number160 hash;
	
	public MusicFile(File file) throws IOException, Exception {
		this.file = file;
		ID3TagReader tagReader = new ID3TagReader();
		this.metadata = tagReader.getMetaData(file);
		this.hash = createHash();
	}
	
	public MusicFile(XMLElement element){
		
	}
	
	@Override
	public MusicMetaData getMetaData() {
		return metadata;
	}
	
	public File getFile() {
		return file;
	}
	
	public boolean isAvailable() {
		return file != null && file.exists();
	}
	
	@Override
	public Number160 getHash() {
		return hash;
	}
	
	private Number160 createHash() throws IOException, Exception {
		HashFunction hf = Hashing.md5();
		HashCode hc = hf.newHasher().putBytes(FileUtils.loadFileIntoByteArray(getFile())).hash();
		Number160 hash = Number160.createHash(hc.toString());
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj instanceof IP2PKeywordElement || obj instanceof MusicFile) {
			IP2PKeywordElement el = (IP2PKeywordElement) obj;
			if(el.getHash().equals(this.getHash())) {
				return true;
			}
		}
		return false;
	}
	
	public XMLElement getXMLElement()
	{
		XMLElement xmlElement = new XMLElement(new Properties(),true,false);
		xmlElement.setName(MusicMetaData.class.getName());

		xmlElement.setAttribute(Constants.FILEHASH_IDENTIFIER, getHash());
		xmlElement.setAttribute(Constants.RATING_IDENTIFIER, metadata.getRating());

		return xmlElement;
		
	}
}
