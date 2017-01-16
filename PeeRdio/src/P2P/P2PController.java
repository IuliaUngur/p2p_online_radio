package P2P;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import P2P.P2PRequest.P2PRequestType;
import P2P.interfaces.IP2PCommand;
import P2P.interfaces.IP2PKeywordElement;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.TrackerData;
import utils.log.LogHandler;
import core.MusicFile;
import core.P2PRadio;
import core.exceptions.BootstrappingFailedException;
import core.exceptions.DownloadFailedException;
import core.exceptions.NotYetConnectedException;
import core.exceptions.SearchFailedException;


public class P2PController implements IP2PCommand {

	private static P2PController instance = null;
	private P2PShare share;
	private RadioPeer radioPeer;
	private ConcurrentLinkedQueue<IP2PKeywordElement> downloadQueue;
	private Thread downloadThread;
	private String nodeId;
	private String trustedNodeIp;
	private P2PCLI p2pcli;
	
	public P2PController() {
		share = new P2PShare(); 
		downloadQueue = new ConcurrentLinkedQueue<IP2PKeywordElement>();
		p2pcli = new P2PCLI(this);
		p2pcli.start();
	}
	
	// singleton like instance control
	public static P2PController getInstance() {
		if(instance == null) {
			instance = new P2PController();
		}
		return instance;
	}
	
	public boolean bootstrap(String trustedNodeIp, String nodeId, boolean isFirstNode, String myIp) throws BootstrappingFailedException {
		this.nodeId = nodeId;
		this.trustedNodeIp = trustedNodeIp;
		LogHandler.info(this, "Bootstrapping with node Id " + nodeId + (isFirstNode == true ? "" : " to trusted node " + trustedNodeIp));
		try {
			radioPeer = new RadioPeer(trustedNodeIp, nodeId, isFirstNode, myIp);
			radioPeer.setShare(share);
			LogHandler.info(this, "radio peer created.");
		} catch (Exception e) {
			e.printStackTrace();
			LogHandler.warning(this, "bootstrapping failed. " + e.getMessage());
			throw new BootstrappingFailedException("Bootstrapping failed. Node Id: " + nodeId + " - trustedNodeIp: " +  trustedNodeIp);
		}
		return false;
	}

	public P2PShare getShare() {
		return share;
	}

	public void publishSharedFiles(final File[] sharedFolders, final File[] sharedFiles) throws NotYetConnectedException {
		if(sharedFolders == null || sharedFiles == null)
			return;
		
		LogHandler.debug(this, "Total sharedFolders: " + sharedFolders.length);
		LogHandler.debug(this, "Total sharedFiles: " + sharedFiles.length);
		
		ensureP2PConnection();
	
		Thread t = new Thread(new Runnable() {
			int count = 0;
			@Override
			public void run() {
				Set<File> folders = new HashSet<File>();
				for(File folder : sharedFolders) {
					if(folder.exists()) {
						folders.add(folder);
					}
				}
				
				HashMap<Number160, MusicFile> files = new HashMap<Number160, MusicFile>();
				for(File file : sharedFiles) {
					count++;
					if(file.exists()) {
						try {
							MusicFile musicFile = new MusicFile(file);
							files.put(musicFile.getHash(), musicFile);
							LogHandler.info(this, "file hashed - " + count + " - " + musicFile.getHash().toString() + " -> " + musicFile.getFile().toString());
						} catch (Exception ex) {
							System.err.println(ex.getMessage());
							continue; // skip this file
						}
					}
				}
				
				share.setSharedFolders(folders);
				share.setSharedFiles(files);
				share.refreshKeywordMap();
				
				try {
					announceAllFiles();
					announceAllKeywords();
				} catch (NotYetConnectedException e) {
					System.err.println(e.getMessage());
				}
			}
		});
		t.start();
	}

	protected void announceAllKeywords() throws NotYetConnectedException {
		ensureP2PConnection();
		try {
			radioPeer.announceKeywords();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void announceAllFiles() throws NotYetConnectedException {
		ensureP2PConnection();
		try {
			radioPeer.announceFiles();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	public RadioPeer getRadioPeer() {
		return radioPeer;
	}

	@Override
	public MusicFile download(Number160 key, boolean saveFile) throws DownloadFailedException, NotYetConnectedException {
			ensureP2PConnection();
			P2PFile downloadedFile = null;
			try {
				
				downloadedFile = getRadioPeer().download(key); // P2PFile has byte array with mp3 file in it.
				
				if(downloadedFile == null) {
					throw new DownloadFailedException("Warning: download of file failed, downloaded file is null.");
				} else {
					LogHandler.info(this, "Received file with filename '" + downloadedFile.getFilename() + "' and byteArray length " + downloadedFile.getData().length);
					if(saveFile) {
						// store file in download folder and return file instance
						MusicFile storedFile = getShare().saveDownloadedSong(downloadedFile); // save file in download folder and add it to shared files
						LogHandler.info(this, "File stored: " + storedFile.getFile());
						if(storedFile.getFile() != null && storedFile.getFile().exists()) {
							getRadioPeer().announceFile(key); // since file exists => announce downloaded file for sharing
						}
						return storedFile;
					}
				}
			
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new DownloadFailedException("Warning: download of file failed, class not found exception: " + e.getMessage(), e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new DownloadFailedException("Warning: download of file failed, IOException: " + e.getMessage(), e);
			} catch (Exception e) {
				e.printStackTrace();
				throw new DownloadFailedException("Warning: download of file failed, Exception: " + e.getMessage(), e);
			}
			
			return null; // saveFile == false, no saving
	}

	@Override
	public List<List<IP2PKeywordElement>> search(SearchDomain searchDomain, String keyword) throws SearchFailedException, NotYetConnectedException {
		ensureP2PConnection();
		ArrayList<Number160> keys = new ArrayList<Number160>();
		ArrayList<String> keywords = new ArrayList<String>();
		ArrayList<String> andWords = new ArrayList<String>();
		for(String str : keyword.split(" ")){
			keywords.add(str.toLowerCase());
		}
		for(int i=0;i<keywords.size();i++){
			if(keywords.get(i).equalsIgnoreCase("&&")){
				if(i-1 >= 0){
					andWords.add(keywords.get(i-1));
				}
				if(i+1 < keywords.size()){
					andWords.add(keywords.get(i+1));
				}
			}
		}
		
		for(String str : keywords){
			keys.add(Number160.createHash(str));
		}
		
		//System.out.println("Number of Keys recognized: "+keys.size());
		//System.out.println("searching for " + searchDomain +  ": '" + keyword + "'");
		List<List<IP2PKeywordElement>> searchResults = new ArrayList<List<IP2PKeywordElement>>();
		
		for(Number160 key : keys){
			try {
				List<IP2PKeywordElement> searchResult = getRadioPeer().search(searchDomain.getHash(), key);
				if(!searchResults.contains(searchResult) ){
					if(andWords.size() > 0){
						List<IP2PKeywordElement> andResults = new ArrayList<IP2PKeywordElement>();
						for(IP2PKeywordElement elem : searchResult){
							String value = elem.getMetaData().getDomain(searchDomain);							
							ArrayList<String> values = new ArrayList<String>();
							for(String str : value.split(" ")){
								values.add(str.toLowerCase());
							}
							if(values.containsAll(andWords)){
								
								andResults.add(elem);
							}
						}
						
						if(andResults.size() > 0)
							searchResults.add(andResults);
						
					} else {
						
						searchResults.add(searchResult);
						
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new SearchFailedException("Warning: search failed, domain: " + searchDomain + ", keyword: " + keyword + ", class not found exception: " + e.getMessage(), e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new SearchFailedException("Warning: search failed, domain: " + searchDomain + ", keyword: " + keyword + ", IOException: " + e.getMessage(), e);
			}
		}

		LogHandler.info(this, "Result of search --> ");
		for(List<IP2PKeywordElement> elements : searchResults) {
			for(IP2PKeywordElement element : elements) {
				LogHandler.info(this, element.getMetaData().toString(false));
			}
		}
		LogHandler.info(this, "<-- Result of search");
		return searchResults;
	}

	@Override
	public Collection<TrackerData> queryTracker(Number160 key) throws NotYetConnectedException {
		ensureP2PConnection();
		LogHandler.info(this, "Send Query to tracker - " + key);
		Collection<TrackerData> trackerDatas = getRadioPeer().queryTracker(key);
		for(TrackerData trackerData:trackerDatas)
		{
			LogHandler.info(this, "Peer " + trackerData + " claims to have the content");
		}
		LogHandler.info(this, "Tracker reports that "+trackerDatas.size()+" peer(s) have this content (" + key + ").");
		return trackerDatas;
	}
	
	
	@Override
	public boolean addToDownloadQueue(IP2PKeywordElement element) throws NotYetConnectedException {
		ensureP2PConnection();
		startDownloadThread();
		return this.downloadQueue.add(element);
	}
	
	private void startDownloadThread() {
		// if thread is already running, we do not have to deal with it.
		if(downloadThread != null && downloadThread.isAlive()) {
			return;
		}
		
		downloadThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					if(downloadQueue.isEmpty()) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					} else {
						IP2PKeywordElement elementToDownload = downloadQueue.poll();
						if(elementToDownload == null) 
							continue;
						try {
							MusicFile downloadedFile = download(elementToDownload.getHash(), true);
							if(downloadedFile.getFile().exists()) {
								P2PRadio.getInstance().getPlaylist().addMusicFileToPlaylist(downloadedFile);
							}
						} catch (DownloadFailedException e) {
							System.err.println("Download failed. " + e.getMessage());
							e.printStackTrace();
						} catch (NotYetConnectedException e) {
							System.out.println(e.getMessage());
						}						
					}
				}
			}
		});
		
		downloadThread.start();
	}

	private void ensureP2PConnection() throws NotYetConnectedException {
		if(radioPeer == null || (radioPeer != null && radioPeer.isRunning() == false)) {
			throw new NotYetConnectedException("You have to connect to the p2p system before you can perform this operation.");
		}
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getTrustedNodeIp() {
		return trustedNodeIp;
	}

	public IP2PKeywordElement[] requestTopTracksFromPeer(
			PeerAddress peerAddress) throws NotYetConnectedException, IOException {
		 ensureP2PConnection();
		return radioPeer.requestTopTracks(peerAddress);
	}

	public double requestCorrelationFromPeer(PeerAddress peerAddress,
			IP2PKeywordElement[] ip2pKeywordElements) throws NotYetConnectedException, IOException {
		 ensureP2PConnection();
		return radioPeer.requestCorrelation(peerAddress, ip2pKeywordElements);
	}

	public void shutdown() {
		if(radioPeer != null)
			radioPeer.shutdown();
	}

	public void clearDownloadQueue() {
		if(downloadQueue != null && !downloadQueue.isEmpty()) {
			downloadQueue.clear();
		}
	}

}
