package P2P;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import P2P.P2PRequest.P2PRequestType;
import P2P.interfaces.IP2PKeywordElement;

import music.prediction.Correlator;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureData;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.futures.FutureTracker;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.p2p.config.ConfigurationGet;
import net.tomp2p.p2p.config.ConfigurationStore;
import net.tomp2p.p2p.config.ConfigurationTrackerStore;
import net.tomp2p.p2p.config.Configurations;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.TrackerData;
import utils.Constants;
import utils.NetworkUtils;
import utils.log.LogHandler;
import core.MusicFile;
import core.P2PRadio;
import core.exceptions.BootstrappingFailedException;
import core.exceptions.NotYetConnectedException;

public class RadioPeer {
	
	private Peer peer;
	private P2PShare share;
	private int port;
	
	public RadioPeer (String trustedNodeIp, String nodeId, boolean isFirstNode, String myIp) throws Exception {
		port = Constants.DEFAULT_P2P_PORT;
		

		
		while(NetworkUtils.isPortAvailable(port) == false) port++;
		
		peer = new PeerMaker(Number160.createHash(nodeId)).setPorts(port).buildAndListen();
	
		if(!isFirstNode) {
			FutureDiscover future = peer.discover(InetAddress.getByName(trustedNodeIp), Constants.DEFAULT_P2P_PORT, Constants.DEFAULT_P2P_PORT);
			future.awaitUninterruptibly();
			FutureBootstrap fb = this.peer.bootstrap(new InetSocketAddress(InetAddress.getByName(trustedNodeIp), Constants.DEFAULT_P2P_PORT));
			fb.awaitUninterruptibly();
			if(fb.isFailed()) {
				throw new Exception("could not connect to node (" + fb.getFailedReason() + ").");
			} else {
				LogHandler.info(this, String.format("bootstrap to known peer successful (%1$2s)", fb.getFailedReason()));
			}
		} else {
			FutureDiscover future = peer.discover(InetAddress.getByName(myIp), Constants.DEFAULT_P2P_PORT, Constants.DEFAULT_P2P_PORT);
			future.awaitUninterruptibly();
			FutureBootstrap fb = peer.bootstrapBroadcast(Constants.DEFAULT_P2P_PORT);
			fb.awaitUninterruptibly();
		}
				
		setReplyHandler();
	}
	
	public void setShare(P2PShare share) {
		this.share = share;
	}
	
	private void setReplyHandler()
	{
		peer.setObjectDataReply(new ObjectDataReply()
		{
			@Override
			public Object reply(PeerAddress sender, Object request) throws Exception
			{
				if(request != null && request instanceof P2PRequest) {
					P2PRequest p2pRequest = (P2PRequest) request;
					P2PRequestType requestType = p2pRequest.getType();
					
					switch(requestType) {
					case DownloadRequest: 
						LogHandler.info(this, "Got a download request from peer " + sender.toString() + " for file (hash): " + (Number160)p2pRequest.getPayload());
						return handleDownloadRequest((Number160)p2pRequest.getPayload());
					case CorrelationRequest: 
						LogHandler.info(this, "Got a correlation request from peer " + sender.toString());
						return handleCorrelationRequest((IP2PKeywordElement[])p2pRequest.getPayload());
					case TopTracksRequest:
						LogHandler.info(this, "Got a top tracks request from peer " + sender.toString());
						return handleTopTracksRequest();
					default: 
						// unknown request
						return null;
					}
					
				}
				else return null;
			}
		});
	}
	
	
	protected P2PFile handleDownloadRequest(Number160 key) throws IOException, Exception {
		if(key == null) return null;
		//returns all values for a given key
		MusicFile musicFile = share.getAll(key);
		P2PFile p2pFile = P2PFile.createP2pFile(musicFile);
		return p2pFile;
	}

	protected double handleCorrelationRequest(IP2PKeywordElement[] requestorsDatabase) {
		if(requestorsDatabase == null) return Double.MAX_VALUE;
		// TODO calculate and return correlation
		Correlator correlator = new Correlator(share.getSharedFiles(),requestorsDatabase);
		correlator.calculateCorrelation();
		
		return correlator.getCorrelation();
	}

	protected IP2PKeywordElement[] handleTopTracksRequest() {
		// TODO get some tracks and return them as an array.
		// just return some random tracks at the moment.
		MusicFile[] files = P2PController.getInstance().getShare().getSharedFiles().values().toArray(new MusicFile[0]);
		Random rnd = new Random();
		int size = Math.min(rnd.nextInt(100), files.length);
		List<IP2PKeywordElement> result = new ArrayList<IP2PKeywordElement>(size);
		for(int i = 0; i < size; i++) {
			result.add(files[i]);
		}
		return result.toArray(new IP2PKeywordElement[0]); // new IP2PKeywordElement[0];
	}

	public void announceFiles() throws IOException
	{
		int count = 0;
		for(Map.Entry<Number160, MusicFile> entry : share.getSharedFiles().entrySet())
		{
			announceFile(entry.getKey());
			count++;
		}
		LogHandler.info(this, "total " + count + " music files announced (tracker).");
	}
	
	public void announceFile(Number160 key) { 
		// we need to announce that we have this piece now
		ConfigurationTrackerStore cts = Configurations.defaultTrackerStoreConfiguration();
		peer.addToTracker(key, cts).awaitUninterruptibly();
	}
	
	public void announceKeywords() throws IOException {
		int count = 0;
		for(Map.Entry<Number160, HashMap<Number160, HashSet<IP2PKeywordElement>>> searchDomain : share.getKeywordsMap().entrySet())
		{
			for(Map.Entry<Number160, HashSet<IP2PKeywordElement>> keyword : searchDomain.getValue().entrySet()) {
				Collection<Data> data = new ArrayList<Data>();
				Iterator<IP2PKeywordElement> it = keyword.getValue().iterator();
				while(it.hasNext()) {
					data.add(new Data(it.next()));
				}
				ConfigurationStore cfg = Configurations.defaultStoreConfiguration();
				cfg.setDomain(searchDomain.getKey());
				peer.add(keyword.getKey(), data, cfg).awaitUninterruptibly();
				count++;
				System.out.println("searchdomain: " + searchDomain.getKey() + " locationKey: " + keyword.getKey());
			}
		}
		LogHandler.info(this, "total " + count + " keywords announced (dht).");
	}
	

	public P2PFile download(Number160 key) throws IOException, ClassNotFoundException, NotYetConnectedException
	{
		Collection<TrackerData> trackerDatas = P2PController.getInstance().queryTracker(key);
		Iterator<TrackerData> it = trackerDatas.iterator();
		FutureData futureData = null;
		P2PFile downloadedSong = null;
		// here we download
		while(it.hasNext()) {
			TrackerData td = it.next();
			LogHandler.info(this, "send download request for file (hash) " + key + " to peer " + td.getPeerAddress().toString()); 
			futureData = send(td.getPeerAddress(), P2PRequestType.DownloadRequest, key);
			futureData.awaitUninterruptibly();
			if(futureData.isSuccess()) {
				downloadedSong = (P2PFile)futureData.getObject();
				if(downloadedSong != null) {
					break; // stop downloading - download succeed and song is not null. otherwise we continue downloading.					
				}
			} else {
				LogHandler.warning(this, "Downloading song from peer failed. Try another peer in order to download file. failed reason was: " + futureData.getFailedReason());
				continue; // downloading
			}
		}
			
		return downloadedSong;
	}

	public FutureData send(PeerAddress address, P2PRequestType type, Object payload) throws IOException {
		return peer.send(address, new P2PRequest(type , payload));
	}

	public Collection<TrackerData> queryTracker(Number160 key) {
		FutureTracker futureTracker = peer.getFromTracker(key, Configurations.defaultTrackerGetConfiguration());
		//now we know which peer has this data, and we also know what other things this peer has
		futureTracker.awaitUninterruptibly();
		Collection<TrackerData> trackerDatas = futureTracker.getTrackers();
		return trackerDatas;
	}
	
	
//	//Lists up every peer in the peermap of this node
//	private void showNeighbours(){
//		
//		Collection<PeerAddress> known = this.peer.getPeerBean().getPeerMap().getAll();
//		Iterator<PeerAddress> iter = known.iterator();
//		System.out.println("I know: ");
//		while(iter.hasNext()){
//			System.out.println(iter.next().toString());
//		}
//		
//	}
	
	
	public List<IP2PKeywordElement> search(Number160 searchDomain, Number160 searchKey) throws ClassNotFoundException, IOException {
		List<IP2PKeywordElement> result = new ArrayList<IP2PKeywordElement>();
		System.out.println("searchdomain: " + searchDomain + " search key: " + searchKey);
		ConfigurationGet cfg = Configurations.defaultGetConfiguration();
		cfg.setDomain(searchDomain);
		FutureDHT future = peer.getAll(searchKey, cfg);
		future.awaitUninterruptibly();
		for(Map.Entry<Number160, Data> entry : future.getDataMap().entrySet())
		{
			IP2PKeywordElement keywordElement = (IP2PKeywordElement)entry.getValue().getObject();
			result.add(keywordElement);
		}
		return result;
	}

	public boolean isRunning() {
		return peer != null && peer.isRunning();
	}
	
	
	public Peer getPeer() {
		return peer;
	}

	public IP2PKeywordElement[] requestTopTracks(PeerAddress peerAddress) throws IOException {
		FutureData futureData = send(peerAddress, P2PRequestType.TopTracksRequest, null);
		futureData.awaitUninterruptibly();
		if(futureData.isSuccess()) {
			IP2PKeywordElement[] result = (IP2PKeywordElement[])futureData.getObject();
			return result;
		}
		return new IP2PKeywordElement[0];
	}

	public double requestCorrelation(PeerAddress peerAddress,
			IP2PKeywordElement[] ip2pKeywordElements) throws IOException {
		FutureData futureData = send(peerAddress, P2PRequestType.CorrelationRequest, ip2pKeywordElements);
		futureData.awaitUninterruptibly();
		if(futureData.isSuccess()) {
			double result = (double)futureData.getObject();
			return result;
		}
		return Double.MAX_VALUE;
	}

	public void shutdown() {
		if(peer != null)
			peer.shutdown();
	}
}
