package P2P;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.TrackerData;
import utils.Constants;
import utils.log.LogHandler;
import P2P.interfaces.IP2PKeywordElement;
import P2P.interfaces.IP2PPlaylistBuilder;
import core.MusicFile;
import core.exceptions.NotYetConnectedException;
import core.exceptions.SearchFailedException;

public class PlaylistBuilder implements IP2PPlaylistBuilder {

	private P2PController p2pController;
	
	public PlaylistBuilder() {
		p2pController = P2PController.getInstance();
	}
	
	
	@Override
	public void buildPlaylist(SearchDomain searchDomain, String searchWord) throws SearchFailedException, NotYetConnectedException, IOException {
		
		LogHandler.info(this, "--> PLAYLIST start building a playlist");
		LogHandler.info(this, "PLAYLIST stop all pending downloads in the download queue.");
		P2PController.getInstance().clearDownloadQueue();
		
		int numberOfratings = calculateNumberOfRatings();		
		LogHandler.info(this, "PLAYLIST number of ratings on this node: " + numberOfratings);
		if(numberOfratings > Constants.MIN_RATINGS_REQUIRED)
			LogHandler.info(this, "PLAYLIST number of ratings > "+Constants.MIN_RATINGS_REQUIRED+" -> build custom playlist with user profile.");
		else
			LogHandler.info(this, "PLAYLIST number of ratings <= "+Constants.MIN_RATINGS_REQUIRED+" -> build custom playlist without user profile.");
		
		// search for tracks
		List<List<IP2PKeywordElement>> searchResult = p2pController.search(searchDomain, searchWord);	
		
		
		if(numberOfratings > Constants.MIN_RATINGS_REQUIRED) {
			
			// select some peers from the search result
			List<PeerAddress> peerAddresses = getPeersForBuildingPlaylist(searchResult, Constants.MAX_PEERS_FOR_BUILDING_PLAYLIST);
			LogHandler.info(this, "PLAYLIST number of selected peers for building playlist: " + peerAddresses.size());
			LogHandler.info(this, "--> peer addresses");
			for(PeerAddress p : peerAddresses) 
				LogHandler.info(this, p.toString());
			LogHandler.info(this, "<-- peer addresses");
			
			// get a list of tracks I like
			List<IP2PKeywordElement> trackDatabase = getRandomTopTracks(Constants.MAX_TRACKS_FOR_TRACKDATABASE);
			LogHandler.info(this, "PLAYLIST selected tracks the user of this node likes: " + trackDatabase.size());
			LogHandler.info(this, "--> tracks");
			for(IP2PKeywordElement t : trackDatabase) 
				LogHandler.info(this, t.getMetaData().toString(false));
			LogHandler.info(this, "<-- tracks");
			
			// send the list of tracks I like to the selected peers and receive the correlation for each peer.
			Map<PeerAddress, Double> correlationPerPeer = getCorrelationFromPeers(peerAddresses, trackDatabase);
			LogHandler.info(this, "PLAYLIST retrieved correlations from peers: " + correlationPerPeer.size());
			LogHandler.info(this, "--> correlation");
			for(PeerAddress p : correlationPerPeer.keySet())
				LogHandler.info(this, "Received from peer: " + p.toString());
			LogHandler.info(this, "<-- correlation");
			
			// select the peers with the best correlation
			List<PeerAddress> playlistBuildingPeers = getBestPeerMatches(correlationPerPeer, Constants.MAX_PEERS_BEST_MATCHES);
			LogHandler.info(this, "PLAYLIST selected peers for retrieving tracks: " + playlistBuildingPeers.size());
			LogHandler.info(this, "--> selected peer");
			for(PeerAddress p : playlistBuildingPeers)
				LogHandler.info(this, p.toString());
			LogHandler.info(this, "<-- selected peers");
			
			// request some songs from the previously selected peers and receive some suggestions from each peer.
			List<IP2PKeywordElement> suggestedTracksFromPeers = requestRandomTopTracksFromPeers(playlistBuildingPeers);
			LogHandler.info(this, "PLAYLIST suggested tracks from selected peers: " + suggestedTracksFromPeers.size());
			LogHandler.info(this, "--> suggested tracks");
			for(IP2PKeywordElement k : suggestedTracksFromPeers)
				LogHandler.info(this, k.getMetaData().toString(false));
			LogHandler.info(this, "<-- suggested tracks");
			
			LogHandler.info(this, "PLAYLIST Add tracks to download queue.");
			// add the received suggestions to the download queue. they will be downloaded and added to the playlist.
			int songsAddedToQueue = 0;
			Collections.shuffle(suggestedTracksFromPeers);
			for(IP2PKeywordElement track : suggestedTracksFromPeers) {
				if(songsAddedToQueue < Constants.MAX_TRACKS_PER_PLAYLIST) {
					p2pController.addToDownloadQueue(track);
					songsAddedToQueue++;
				} else {
					break;
				}
			}
		
		} else {
			
			LogHandler.info(this, "--> selected tracks");
			int songsAddedToQueue = 0;
			for(List<IP2PKeywordElement> list : searchResult) {
				for(IP2PKeywordElement elem : list){
					if(songsAddedToQueue < Constants.MAX_TRACKS_PER_PLAYLIST) {
						LogHandler.info(this, elem.getMetaData().toString(false));
						p2pController.addToDownloadQueue(elem);
						songsAddedToQueue++;
					} else {
						break;
					}
				}
			}
			LogHandler.info(this, "<-- selected tracks");
			
		}
		
		LogHandler.info(this, "<-- PLAYLIST building playlist finished");
	}


	private int calculateNumberOfRatings() {
		int ratings = 0;
		HashMap<Number160, MusicFile> fileMap = p2pController.getShare().getSharedFiles();
		for(MusicFile file : fileMap.values()){
			if(file.getMetaData().isRated())
				ratings++;
		}
		return ratings;
	}
	
		
	@Override
	public List<PeerAddress> getPeersForBuildingPlaylist(
			List<List<IP2PKeywordElement>> trackList, int maxNumberOfPeers) throws NotYetConnectedException {
		
		System.out.println("--> getPeersForBuildingPlaylist");
		List<PeerAddress> result = new ArrayList<PeerAddress>();
		// get all peer addresses for all file hashes (keys)
		for(List<IP2PKeywordElement> list : trackList) {
			for(IP2PKeywordElement element : list){
				Collection<TrackerData> peers = p2pController.queryTracker(element.getHash());
				for(TrackerData data : peers) {
					if(!data.getPeerAddress().equals(p2pController.getRadioPeer().getPeer().getPeerAddress()) && !result.contains(data.getPeerAddress())) { // do not add our peer and every peer max. once
						result.add(data.getPeerAddress());
					}
				}
			}
		}
		
		// remove random peers until we reach the requested size
		Random rnd = new Random();
		while(result.size() > maxNumberOfPeers) {
			result.remove(rnd.nextInt(result.size()));
		}
		System.out.println("getPeersForBuildingPlaylist -->");
		return result;
	}

	@Override
	public List<IP2PKeywordElement> getRandomTopTracks(
			int numberOfTracksToReturn) {
		// ! this is on the local peer ! -> songs will be sent to the chosen peer
		// TODO build a track dictionary
		
		System.out.println("--> getRandomTopTracks");
		SortedMap<Float, List<MusicFile>> sortedSongs = new TreeMap<Float, List<MusicFile>>();
		for(MusicFile mf : p2pController.getShare().getSharedFiles().values()) {
			float rating = mf.getMetaData().getRating();
			if(!sortedSongs.containsKey(rating)) {
				sortedSongs.put(rating, new ArrayList<MusicFile>());
			}
			sortedSongs.get(rating).add(mf);
		}
		
		List<IP2PKeywordElement> orderedTracks = new ArrayList<IP2PKeywordElement>();
		for(Float f : sortedSongs.keySet()) {
			orderedTracks.addAll(sortedSongs.get(f));
		}
		
		List<IP2PKeywordElement> topTracks = new ArrayList<IP2PKeywordElement>(); 
		Random rnd = new Random();
		int numberOfMaxLoops = 0;
		
		while(numberOfMaxLoops < 100) {
			int index = orderedTracks.size()/2 + rnd.nextInt(orderedTracks.size()/2) -1; // upper half.
			if(index >= 0 && index < orderedTracks.size()) {
				if(!topTracks.contains(orderedTracks.get(index))) {
					topTracks.add(orderedTracks.get(index));
				}
			}
			
			if(orderedTracks.size() > numberOfTracksToReturn)
				break;
			
			numberOfMaxLoops++;
		}
		System.out.println("getRandomTopTracks -->");
		return topTracks;
	}

	@Override
	public Map<PeerAddress, Double> getCorrelationFromPeers(
			List<PeerAddress> peerAddresses,
			List<IP2PKeywordElement> trackDatabase) throws NotYetConnectedException, IOException {
		
		System.out.println("--> getCorrelationFromPeers");
		Map<PeerAddress, Double> result = new HashMap<PeerAddress, Double>();
		for(PeerAddress address : peerAddresses) {
			double corr = p2pController.requestCorrelationFromPeer(address, trackDatabase.toArray(new IP2PKeywordElement[0]));
			result.put(address, corr);
		}
		
		System.out.println("getCorrelationFromPeers -->");
		return result;
	}

	@Override
	public double calculateCorrelation(List<IP2PKeywordElement> trackDatabase) {
		// brauchts wohl nicht mehr -> handleCorrelationRequest in RadioPeer ist die antwort des peers.
		System.out.println("--> calculateCorrelation");
		System.out.println("calculateCorrelation -->");
		return Double.MAX_VALUE;
	}

	@Override
	public List<PeerAddress> getBestPeerMatches(
			Map<PeerAddress, Double> peerCorrelations, int maxNumberOfElements) {
		
		System.out.println("--> getBestPeerMatches");
		
		// to hold the result
		List<PeerAddress> result = new ArrayList<PeerAddress>();

		List<PeerAddress> mapKeys = new ArrayList<PeerAddress>(peerCorrelations.keySet());
		List<Double> mapValues = new ArrayList<Double>(peerCorrelations.values());
		
		TreeSet<Double> sortedSet = new TreeSet<Double>(mapValues);
		Object[] sortedArray = sortedSet.toArray();
		
		int maxPossibleElements = Math.min(sortedArray.length, maxNumberOfElements);
		for (int i = 0; i < maxPossibleElements; i++) {
			result.add(mapKeys.get(mapValues.indexOf(sortedArray[i])));
		}
		
		System.out.println("getBestPeerMatches -->");
		return result;
	}

	@Override
	public List<IP2PKeywordElement> getRandomTopSongs(int numberOfSongs) {
		// ! this is on the remote peer ! -> songs will be sent back to the requester
		// TODO build track database and sent back songs.
		// brauchts wohl nicht mehr -> handleTopTracksRequest in RadioPeer verwenden (antwort des peers)
		System.out.println("--> getRandomTopSongs");
		
			
		
		System.out.println("getRandomTopSongs -->");
		return new ArrayList<IP2PKeywordElement>();
	}

	@Override
	public List<IP2PKeywordElement> requestRandomTopTracksFromPeers(
			List<PeerAddress> peerList) throws NotYetConnectedException, IOException {
		
		System.out.println("--> requestRandomTopTracksFromPeers");
		
		List<IP2PKeywordElement> result = new ArrayList<IP2PKeywordElement>();
		for(PeerAddress peerAddress : peerList) {
			IP2PKeywordElement[] res = p2pController.requestTopTracksFromPeer(peerAddress);
			for(IP2PKeywordElement element : res) {
				result.add(element);
			}
		}
		System.out.println("requestRandomTopTracksFromPeers -->");
		return result;
	}



}
