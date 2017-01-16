package P2P;

import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import P2P.interfaces.IP2PKeywordElement;

import music.metadatacrawler.MusicMetaData;
import net.tomp2p.peers.Number160;
import core.MusicFile;
import core.P2PRadio;
import core.exceptions.DownloadFailedException;
import core.exceptions.NotYetConnectedException;
import core.exceptions.SearchFailedException;


public class P2PCLI extends Thread {

	// controller instance - access to radio peer, p2pshare, etc.
	private P2PController controller;

	public P2PCLI(P2PController controller)	{
		super();
		this.controller = controller;
	}
	
	@Override
	public synchronized void start() {
		super.start();
		System.out.println("P2P CLI Started. Waiting for commands...");
	}

	@Override
	public void run() {
		super.run();
		Scanner input = new Scanner(System.in);
		String command;
		while((command = input.nextLine()) != null) {
			try { 
				String[] args = command.trim().toLowerCase().split(" ");
				if(args.length > 0) {
					
					/**
					 * Request song and download it from a peer, but do NOT store song after downloading.
					 */
					if(args[0].equalsIgnoreCase("download")) {
						download(args, false);
					
					} 
					/**
					 * Downloda song and store it in the download folder. song will be added to P2P share (keywords, shared files)
					 */
					else if(args[0].equalsIgnoreCase("downloadAndStore")) { 
						download(args, true);
					} 
					/**
					 * Downloda song and store it in the download folder. song will be added to P2P share (keywords, shared files)
					 */
					else if(args[0].equalsIgnoreCase("addToDownloadQueue")) { 
						addToDownloadQueue(args);
					} 
					/**
					 * Send request to tracker and receive list of peers which have the song / key
					 */
					else if(args[0].equalsIgnoreCase("queryTracker")) {
						queryTracker(args);
					} 
					/**
					 * Search for songs by artist
					 */
					else if(args[0].equalsIgnoreCase("searchArtist")) {
						search(SearchDomain.ARTIST, args);
					} 
					/**
					 * Search for songs by album
					 */
					else if(args[0].equalsIgnoreCase("searchAlbum")) {
						search(SearchDomain.ALBUM, args);
					} 
					/**
					 * Search for songs by genre
					 */
					else if(args[0].equalsIgnoreCase("searchGenre")) {
						search(SearchDomain.GENRE, args);
					} 
					/**
					 * Search for songs by Song (= title, filename)
					 */
					else if(args[0].equalsIgnoreCase("searchSong")) {
						search(SearchDomain.SONG, args);
					} 
					/**
					 * list the files in the sharedFiles map. these are all files, that are available on this machine (local!)
					 */
					else if(args[0].equalsIgnoreCase("listsharedfiles")) { 
						 listsharedfiles(args);
					}
					else if(args[0].equalsIgnoreCase("playsong")){
						playSong(args);
					}
					/**
					 * Unknown command
					 */
					else {
						throw new Exception("Unknown command.");
					}
				}
			/**
			 * Exception - ! maybe some arguments are missing !	
			 */
			} catch(NotYetConnectedException notConnectedEx) {
				System.out.println(notConnectedEx.getMessage());
			} catch(Exception ex) { System.out.println("CMD Failed with Exception message: " + ex.getMessage()); }
		}
	}

	private void addToDownloadQueue(final String[] args) throws NotYetConnectedException {
		// big hack, but we deal with user input... so we only have strings and not objects.
		controller.addToDownloadQueue(new IP2PKeywordElement() {
			private static final long serialVersionUID = 1L;

			@Override
			public MusicMetaData getMetaData() {
				return null;
			}
			
			@Override
			public Number160 getHash() {
				return new Number160(args[1]);
			}
		});
	}

	private void queryTracker(String[] args) throws NotYetConnectedException {
		controller.queryTracker(new Number160(args[1]));
	}

	private void search(SearchDomain domain, String[] args) throws SearchFailedException, NotYetConnectedException {
		String argument = "";
		for(int i = 1; i < args.length; i++) {
			argument += args[i] + " ";
		}
		controller.search(domain, argument.trim());		
	}

	private void download(String[] args, boolean saveFile) throws Exception {
		controller.download(new Number160(args[1]), saveFile);
	}

	private void listsharedfiles(String[] args) {
		Set<Entry<Number160, MusicFile>> entrySet = controller.getShare().getSharedFiles().entrySet();
		 for(Entry<Number160, MusicFile> element : entrySet) {
			 System.out.println(element.getKey().toString() + " - " + element.getValue().getFile().getAbsolutePath());
		 }
	}
	
	private void playSong(String[] args) throws DownloadFailedException, NotYetConnectedException{
		MusicFile musicfile = controller.download(new Number160(args[1]), true);
		if(musicfile != null) {
			P2PRadio.getInstance().getPlaylist().addMusicFileToPlaylist(musicfile);
		}
	}

}
