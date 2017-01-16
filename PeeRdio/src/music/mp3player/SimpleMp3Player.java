package music.mp3player;

import java.util.Map;

import utils.Constants;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import utils.log.LogHandler;

import core.MusicFile;
import core.PlayList;

public class SimpleMp3Player implements BasicPlayerListener {
  
  private BasicPlayer player;
  private PlayList playlist;
  private int currentSongIndex;
  private MusicFile currentSong;
  private boolean isPaused = false;
  
  public SimpleMp3Player(){
	  player = new BasicPlayer();
	  player.addBasicPlayerListener(this);
	  currentSongIndex = -1;
  }
  
  public void play() throws BasicPlayerException{
	  if(isPaused) {
		  player.resume();
	  } else {
		  player.play();
	  }
	  isPaused = false;
  }
    
  public void stop() throws BasicPlayerException{
	  player.stop();
  }
  
  public void pause() throws BasicPlayerException{
	  isPaused = true;
	  player.pause();
  }
  
  private void playSong(MusicFile musicFile) throws BasicPlayerException{
	  player.stop();
	  currentSong = musicFile;
	  player.open(musicFile.getFile());
	  player.play();
	  LogHandler.info(this, "Currently playing: " + musicFile.getMetaData().toString(false));
  }

	
	public void endOfMedia() throws BasicPlayerException {
		// play next song in playlist
		if(isPaused == false) {
			if(playlist.getRowCount() > 0) {
				playlist.rateSong(currentSong, Constants.RATING_PLAY);
				playNextSong();
			}
		}
	}
	
	public void playNextSong() throws BasicPlayerException {
		if(playlist.getRowCount() > 0) {
			currentSongIndex = (currentSongIndex + 1) % playlist.getRowCount();
			playSong(currentSongIndex);
			LogHandler.info(this,  "Mp3Player: playing next song " + currentSongIndex + "/" + playlist.getRowCount());
		}
	}
	
	public void playPreviousSong() throws BasicPlayerException {
		if(playlist.getRowCount() > 0) {
			currentSongIndex = currentSongIndex - 1;
			if(currentSongIndex < 0) {
				currentSongIndex = playlist.getRowCount()-1;
			}
			playSong(currentSongIndex);
			LogHandler.info(this, "Mp3Player: playing previous song " + currentSongIndex + "/" + playlist.getRowCount());
		}
	}
	
	
	public PlayList getPlaylist() {
		return playlist;
	}

	public void setPlaylist(PlayList playlist) {
		this.playlist = playlist;
	}

	public void playSong(int index) throws BasicPlayerException {
		if(index < 0) {
			if(playlist.getRowCount() > 0) {
				index = 0; // play first song
			} else {
				return; // nothing to play
			}
		}
		else if(index >= playlist.getRowCount()) {
			return;
		}
		
		playSong(playlist.getMusicFile(index));
		currentSongIndex = index;
		playlist.getPlaylistUI().setCurrentIndex(index);
	}

	public void isPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}
	
	public boolean isPaused() {
		return isPaused;
	}
	
	
	public MusicFile getCurrentSong() {
		return currentSong;
	}

	@Override
	public void opened(Object arg0, Map arg1) {
		
	}

	@Override
	public void progress(int arg0, long arg1, byte[] arg2, Map arg3) {
			
	}

	@Override
	public void setController(BasicController arg0) {
		
	}

	@Override
	public void stateUpdated(BasicPlayerEvent arg0) {
		System.out.println("Player state update - " + arg0.getCode());
		switch(arg0.getCode()) {
		case BasicPlayerEvent.EOM: 
			try {
				endOfMedia();
			} catch (BasicPlayerException e) { e.printStackTrace(); } 
			break;
		default:
			break;
		}
		
	}

	public void addPlayerListener(BasicPlayerListener frame) {
		this.player.addBasicPlayerListener(frame);
	}
  
	
}
