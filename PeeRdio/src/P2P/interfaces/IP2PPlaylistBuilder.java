package P2P.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import P2P.SearchDomain;

import core.exceptions.NotYetConnectedException;
import core.exceptions.SearchFailedException;

import net.tomp2p.peers.PeerAddress;


/**
 *
 * Playlist Building Process:
 * [*] user enters a keyword and starts search process by click on a button (artist, genre, ...) --> requester
 * [*] application builds playlist in the following way:
 * 
 * 		1. requester: 	->	get file hashes according to keyword entered by user
 * 		2. requester: 	->	select X random file hashes from the list. 
 * 		3. requester: 	->	select Y random peers, which share the previously selected files --> chosen peers
 * 		4. requester: 	->	build track database with our top tracks.
 * 		5. requester	->  contact the random peers and send track database to the chosen peers
 * 		6. chosen peer: ->	calculates the correlation and sends it back to the requester.
 * 		7. requester: 	->	selects Z peers which have the lowest (?) correlation
 * 		8. requester: 	->  request songs from the peers with the lowest correlation. 
 * 		9. chosen peer: ->	sends back K random selected songs from the top half of his songs (depending on rating).
 * 		10. requester:	->	adds songs to download queue (initiates download process and playing the songs)
 * 
 */

public interface IP2PPlaylistBuilder {
	
	/**
	 * @throws NotYetConnectedException 
	 * @throws SearchFailedException 
	 * @throws IOException 
	 * 
	 */
	void buildPlaylist(SearchDomain searchDomain, String searchWord) throws SearchFailedException, NotYetConnectedException, IOException;
	
	/**
	 * Step 1: see IP2PCommand Interface, call search()
	 * --> List<IP2PKeywordElement> search(SearchDomain searchDomain, String keyword)
	 */
	
	/**
	 * Step 2 / 3: from a list of IP2PKeywordElements, select some random peers and get their peer addresses and return a list of addresses
	 * @param tackList a list of tracks which are relevant to the keyword the user entered in the search field
	 * @param maxNumberOfPeers the maximum number of peers of the resulting list (return)
	 * @return 
	 * @throws NotYetConnectedException 
	 */
	List<PeerAddress> getPeersForBuildingPlaylist(List<List<IP2PKeywordElement>> trackList, int maxNumberOfPeers) throws NotYetConnectedException;
	
	/**
	 * Step 4: 
	 * using tracks saved in P2PShare. 
	 * @param numberOfTracksToReturn the maximum number of tracks to return
	 */
	List<IP2PKeywordElement> getRandomTopTracks(int numberOfTracksToReturn);
	
	/**
	 * Step 5
	 * @param peerAddresses a list of previously selected peer addresses (chosen peers)
	 * @param trackDatabase a list of previously selected tracks (some random tracks the user likes, already downloaded in the past)
	 * @throws NotYetConnectedException 
	 * @throws IOException 
	 */
	Map<PeerAddress, Double> getCorrelationFromPeers(List<PeerAddress> peerAddresses, List<IP2PKeywordElement> trackDatabase) throws NotYetConnectedException, IOException;
	
	/** 
	 * Step 6
	 * forwarding this request to the prediction / rating module?
	 */
	double calculateCorrelation(List<IP2PKeywordElement> trackDatabase);
	
	/**
	 * Step 7
	 * @param peerCorrelations a map of the form (peeraddress, correlation) -> this is the result of the getCorrelationFromPeers() call
	 * @param maxNumberOfElements max number of elements in the return map
	 */
	List<PeerAddress> getBestPeerMatches(Map<PeerAddress, Double> peerCorrelations, int maxNumberOfElements);
	
	/**
	 * 
	 * @param peerList a list of peer addresses with best correlation (list is the keyset of the return of step 7.)
	 * @return a list of keywordelements which the chosen peers sent to the requester
	 * @throws NotYetConnectedException 
	 * @throws IOException 
	 */
	List<IP2PKeywordElement> requestRandomTopTracksFromPeers(List<PeerAddress> peerList) throws NotYetConnectedException, IOException;
	
	/** 
	 * Step 9
	 * @param numberOfSongs the maximum number of songs in the resulting list (return)
	 */
	List<IP2PKeywordElement> getRandomTopSongs(int numberOfSongs);
	
	
	/**
	 * Step 10: see IP2PCommand Interface, call addToDownloadQueue()
	 * --> boolean addToDownloadQueue(IP2PKeywordElement element)
	 */
	
}
