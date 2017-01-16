package P2P;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.tomp2p.peers.Number160;
import utils.Constants;
import utils.FileUtils;
import utils.log.LogHandler;

import P2P.interfaces.IP2PKeywordElement;

import com.google.common.base.Joiner;

import core.MusicFile;

public class P2PShare {
	
	private Set<File> sharedFolders;
	private HashMap<Number160, MusicFile> sharedFiles;
	private HashMap<Number160, HashMap<Number160, HashSet<IP2PKeywordElement>>> keywordMap; // 1st hash of search domain (e.g. Number160 of GENRE), 2nd hash of keyword (e.g. Number160(Rock), 3rd hash of file (=same as key of sharedFiles). 
	private File downloadPath;
	
	public P2PShare() {
		sharedFolders = new HashSet<File>();
		sharedFiles = new HashMap<Number160, MusicFile>();
		downloadPath = new File(new File("").getAbsolutePath(), Constants.DOWNLOADED_FILES_FOLDER);
		
		initKeywordMap();
		
	}
		
	private void initKeywordMap() {
		keywordMap = new HashMap<Number160, HashMap<Number160, HashSet<IP2PKeywordElement>>>();
		for(SearchDomain domain : SearchDomain.values()) {
			keywordMap.put(domain.getHash(), new HashMap<Number160, HashSet<IP2PKeywordElement>>()); // make empty hashMap for each search domain.
		}
		
	}
	
	private HashMap<Number160, HashSet<IP2PKeywordElement>> getKeywordMap(SearchDomain domain) {
		return keywordMap.get(domain.getHash());
	}
	

	public File getDownloadPath() {
		return downloadPath;
	}
	
	public void setDownloadPath(File path) {
		this.downloadPath = path;
	}
	
	public Set<File> getSharedFolders() {
		return this.sharedFolders;
	}

	public void setSharedFolders(Set<File> sharedFolders) {
		this.sharedFolders = sharedFolders;
	}
	
	public HashMap<Number160, MusicFile> getSharedFiles() {
		return this.sharedFiles;
	}
	
	public void setSharedFiles(HashMap<Number160, MusicFile> sharedFiles) {
		this.sharedFiles = sharedFiles;
	}
	
	public void addMusicFileToKeywordMap(MusicFile musicFile) {
		addMusicFileToGenreDomain(musicFile);
		addMusicFileToArtistDomain(musicFile);
		addMusicFileToAlbumDomain(musicFile);
		addMusicFileToTitleDomain(musicFile);
	}
	
	private  Collection<String> deletion(String word)
	{
		Set<String> resultSet = new HashSet<String>();
		resultSet.add(word);
		StringBuilder sb = new StringBuilder(word);
		for(int i=0;i<word.length();i++)
		{
			char c = sb.charAt(i);
			sb.deleteCharAt(i);
			resultSet.add(sb.toString());
			sb.insert(i, c);
		}
		return resultSet;
	}

	private void addMusicFileToGenreDomain(MusicFile musicFile) {
		String genre = musicFile.getMetaData().getGenre().toLowerCase();
		for(String deletion : deletion(genre)){
			Number160 keyword = Number160.createHash(deletion);
			addToKeywordMap(SearchDomain.GENRE, keyword, musicFile);
			LogHandler.info(this, Joiner.on("\t").join(SearchDomain.GENRE.toString(), deletion + "( " + keyword + " )", "musicfile( " + musicFile.getHash() +" )"));
		}

	}
	
	private void addMusicFileToAlbumDomain(MusicFile musicFile) {
		Iterator<String> it = musicFile.getMetaData().getAlbumAsKeywords().iterator();
		while(it.hasNext()) {
			String album = it.next().toLowerCase();
			for(String deletion : deletion(album)){
				Number160 keyword = Number160.createHash(deletion);
				addToKeywordMap(SearchDomain.ALBUM, keyword, musicFile);
				LogHandler.info(this, Joiner.on("\t").join(SearchDomain.ALBUM.toString(), deletion + "( " + keyword + " )", "musicfile( " + musicFile.getHash() + " )"));
			}

		}
	}
	
	private void addMusicFileToArtistDomain(MusicFile musicFile) {
		Iterator<String> it = musicFile.getMetaData().getArtistAsKeywords().iterator();
		while(it.hasNext()) {
			String artist = it.next().toLowerCase();
			for(String deletion : deletion(artist)){
				Number160 keyword = Number160.createHash(deletion);
				addToKeywordMap(SearchDomain.ARTIST, keyword, musicFile);
				LogHandler.info(this, Joiner.on("\t").join(SearchDomain.ARTIST.toString(), deletion + "( " + keyword + " )", "musicfile( " + musicFile.getHash() + " )"));
			}
		}
	}
	
	private void addMusicFileToTitleDomain(MusicFile musicFile) {
		Iterator<String> it = musicFile.getMetaData().getTitleAsKeywords().iterator();
		while(it.hasNext()) {
			String song = it.next().toLowerCase();
			for(String deletion : deletion(song)){
				Number160 keyword = Number160.createHash(deletion);
				addToKeywordMap(SearchDomain.SONG, keyword, musicFile);
				LogHandler.info(this, Joiner.on("\t").join(SearchDomain.SONG.toString(), deletion + "( " + keyword + " )", "musicfile( " + musicFile.getHash() + " )"));
			}
		}
 	}
	
	private void addToKeywordMap(SearchDomain searchDomain, Number160 keyword, IP2PKeywordElement keywordElement) {
		if(keyword == null || keywordElement == null) return;
		if(!getKeywordMap(searchDomain).containsKey(keyword)) {
			getKeywordMap(searchDomain).put(keyword, new HashSet<IP2PKeywordElement>());
		}
		getKeywordMap(searchDomain).get(keyword).add(keywordElement);
	}

	public void refreshKeywordMap() {
		LogHandler.info(this, "Creating keywords mapping.");
		for(HashMap<Number160, HashSet<IP2PKeywordElement>> map : keywordMap.values()) {
			map.clear();
		}
		for(MusicFile mf : sharedFiles.values()) {
			addMusicFileToKeywordMap(mf);
		}
		LogHandler.info(this, "Keywords mapping finished.");
		LogHandler.debug(this, "Keyword Map refreshed. Keyword map contains " + keywordMap.size() + " elements.");
	}

	public MusicFile getAll(Number160 key) {
		return sharedFiles.get(key);
	}

	public MusicFile saveDownloadedSong(P2PFile p2pfile) throws IOException, Exception  {
		String filename = downloadPath + File.separator + p2pfile.getFilename();
		File file = new File(filename);
		
		// in case file already exists, we change the filename
		int index = 1;
		while(file.exists()) {
			filename = downloadPath + File.separator + p2pfile.getFilename();
			int lastIdx = filename.lastIndexOf(".");
			filename = filename.substring(0, lastIdx) + "_" + index + filename.substring(lastIdx);
			file = new File(filename);
			index++;
		}
		
		FileUtils.saveFileFromByteArray(filename, p2pfile.getData());
		MusicFile musicFile = new MusicFile(file);
		addMusicFileToKeywordMap(musicFile);
		sharedFiles.put(musicFile.getHash(), musicFile);
		return musicFile;
	}

	public Map<Number160, HashMap<Number160, HashSet<IP2PKeywordElement>>> getKeywordsMap() {
		return keywordMap;
	}

}
