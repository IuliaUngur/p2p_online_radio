package core;

import gui.MainFrame;

import java.awt.EventQueue;

import javax.swing.JFrame;

import music.mp3player.SimpleMp3Player;
import net.tomp2p.peers.Number160;

import P2P.P2PController;

public class P2PRadio {

	private static P2PRadio instance = null;
	private MainFrame frame;
	private PlayList playlist;
	private SimpleMp3Player mp3Player;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					P2PRadio p2pRadio = P2PRadio.getInstance();
					p2pRadio.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	// singleton like instance control
	public static P2PRadio getInstance() {
		if(instance == null) {
			instance = new P2PRadio();
		}
		return instance;
	}
		
	/**
	 * Create the application.
	 */
	public P2PRadio() {
		initialize();
		initPlaylist();
		initPlayer();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new MainFrame();
		P2PController.getInstance(); // just creates instance, nothing to do...
		
		frame.setBounds(100, 100, 900, 600);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	private void initPlaylist() {
		playlist = new PlayList();
		frame.setPlaylist(playlist);
		playlist.setPlaylistUI(frame.getPlaylistUI());
	}


	private void initPlayer() {
		mp3Player = new SimpleMp3Player();
		mp3Player.setPlaylist(playlist);
		mp3Player.addPlayerListener(frame);
	}


	public PlayList getPlaylist() {
		return playlist;
	}


	public SimpleMp3Player getMp3Player() {
		return mp3Player;
	}

}
