package music.metadata;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

public class MusicMetaData {
	
	private String _artist;
	private String _album;
	private String _title;
	private String _genre;
	private int _trackLength;
	private String _fileName;
	private long _fileSize;
	
	public MusicMetaData() {
		_artist 		= "";
		_album 			= "";
		_title 			= "";
		_genre 			= "";
		_trackLength 	= 0;
		_fileName 		= "";
		_fileSize 		= 0;
	}
	
	public String getArtist() {
		return _artist;
	}
	
	public Iterable<String> getArtistAsKeywords() {
		return cleanup(createKeywords(_artist)); //cleanup(_artist.split(" "));
	}

	public void setArtist(String artist) {
		_artist = artist;
	}
	
	public boolean hasArtist() {
		return _artist != null && !_artist.isEmpty();
	}
	
	public String getAlbum() {
		return _album;
	}
	
	public Iterable<String> getAlbumAsKeywords() {
		return cleanup(createKeywords(_album));
	}
	
	public void setAlbum(String album) {
		_album = album;
	}
	
	public boolean hasAlbum() {
		return _album != null && !_album.isEmpty();
	}
	
	public String getTitle() {
		return _title;
	}
	
	public Iterable<String> getTitleAsKeywords() {
		return cleanup(createKeywords(_title));
	}
	
	public void setTitle(String title) {
		_title = title;
	}
	
	public boolean hasTitle() {
		return _title != null && !_title.isEmpty();
	}
	
	public String getGenre() {
		return _genre;
	}
	
	public void setGenre(String genre) {
		_genre = genre;
	}
	
	public boolean hasGenre() {
		return _genre != null && !_genre.isEmpty();
	}
	
	public int getTrackLength() {
		return _trackLength;
	}
	
	public void setTrackLength(int trackLength) {
		_trackLength = trackLength;
	}
	
	public boolean hasTrackLength() {
		return _trackLength > 0;
	}
	
	public String getFileName() {
		return _fileName;
	}
	
	public Iterable<String> getFileNameAsKeywords() {
		return cleanup(createKeywords(_fileName));
	}
	
	public void setFileName(String fileName)
	{
		_fileName = fileName;
	}
	
	public boolean hasFileName() {
		return _fileName != null && !_fileName.isEmpty();
	}
	
	public long getFileSize() {
		return _fileSize;
	}
	
	public void setFileSize(long size) {
		_fileSize = size;
	}
	
	public boolean hasFileSize() {
		return _fileSize > 0;
	}
	
	private Iterable<String> createKeywords(String s) {
		return Splitter.on(CharMatcher.anyOf(";,. -_+")).trimResults().omitEmptyStrings().split(s);
	}
	
	private Iterable<String> cleanup(Iterable<String> iterable) {
		return iterable; // TODO: clean array and remove short and common words
	}
	
	private String iterableToString(Iterable<String> iterable) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(String s : iterable) {
			sb.append(s).append(", ");
		}
		sb.delete(sb.lastIndexOf(", "), sb.length());
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Artist: "); 
		sb.append(_artist);
		sb.append(" ").append(iterableToString(getArtistAsKeywords()));
		sb.append("\n");
		
		sb.append("Album: "); 
		sb.append(_album);
		sb.append(" ").append(iterableToString(getAlbumAsKeywords()));
		sb.append("\n");
		
		sb.append("Title: "); 
		sb.append(_title);
		sb.append(" ").append(iterableToString(getTitleAsKeywords()));
		sb.append("\n");
		
		sb.append("Genre: "); 
		sb.append(_genre);
		sb.append("\n");
		
		sb.append("Track Length (seconds): "); 
		sb.append(_trackLength);
		sb.append("\n");
		
		sb.append("File Name: "); 
		sb.append(_fileName);
		sb.append(" ").append(iterableToString(getFileNameAsKeywords()));
		sb.append("\n");
		
		sb.append("File Size (Bytes): "); 
		sb.append(_fileSize);
		sb.append("\n");
		
		return sb.toString();
	}

	
}
