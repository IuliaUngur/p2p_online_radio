package core;

import gui.PlaylistUI;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import utils.log.LogHandler;


import music.metadatacrawler.MusicMetaData;

public class PlayList extends AbstractTableModel  {
	
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {
									"#",
								    "Artist",
								    "Album",
								    "Title",
								    "Genre"
								    };
	
	
	private List<MusicFile> playList;
	private PlaylistUI playlistUI;
	
	public PlayList() {
		playList = new ArrayList<MusicFile>();
	}
	
	
	public void clearPlaylist()	{
		playList.clear();
		fireTableDataChanged();
	}
	
	public void addMusicFileToPlaylist(MusicFile musicFile) {
		playList.add(musicFile);
		fireTableDataChanged();
	}
	
	public MusicFile getMusicFile(int index) {
		if(index >= 0 && index < playList.size()) {
			return playList.get(index);
		} 
		return null;
	}


	@Override
	public int getRowCount() {
		return playList.size();
	}


	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(int col) {
        return columnNames[col];
    }


	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		MusicMetaData md = playList.get(rowIndex).getMetaData();
		switch(columnIndex) {
		case 0: 
			return rowIndex+1; 
		case 1: 
			return md.hasArtist() ? md.getArtist() : "unknown"; 
		case 2: 
			return md.hasAlbum() ? md.getAlbum() : "unknown"; 
		case 3: 
			return md.hasTitle() ? md.getTitle() : md.getFileName(); 
		case 4: 
			return md.hasGenre() ? md.getGenre() : "unknown"; 
		default:
			return "unknown";
		}
	}
	
	public PlaylistUI getPlaylistUI() {
		return this.playlistUI;
	}
	
	
	public void setPlaylistUI(PlaylistUI playlistUI) {
		this.playlistUI = playlistUI;
	}


	public void rateCurrentSelectedSong(int rating) {
		if(playlistUI == null) return;
		MusicFile mf = getMusicFile(playlistUI.getCurrentIndex());
		rateSong(mf, rating);
	}
	
	public void rateSong(MusicFile mf, int rating) {
		if(mf == null) return;
		
		mf.getMetaData().setRating(rating);
		LogHandler.info(this, "Rate song " + mf.getMetaData().toString(false) + " with rating " + rating);
	}
}
