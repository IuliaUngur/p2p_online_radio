package P2P.interfaces;

import java.util.Collection;
import java.util.List;

import core.MusicFile;
import core.exceptions.DownloadFailedException;
import core.exceptions.NotYetConnectedException;
import core.exceptions.SearchFailedException;

import P2P.SearchDomain;

import net.tomp2p.peers.Number160;
import net.tomp2p.storage.TrackerData;

public interface IP2PCommand {
	
	
	/**
	 * SEARCH and DOWNLOAD is working as follows:
	 * - search for a keyword, e.g. "metallica". Since this is an artist, we use the search domain SearchDomain.ARTIST. 
	 * - the search(...) call will return a list containing IP2PKeywordElement objects. Each element contains the hash of the file and the metadata. 
	 * - therefore, you know WHICH files match with the given search keyword since you have the metadata.
	 * - in order to download a file, use the hash of the file in the IP2PKeywordElement for a download request. 
	 * - queryTracker(...) just queries the tracker and returns the peers claiming to have the file. It does not download any file from any peer.
	 * - download(...) is used to download a file. Use the saveFile boolean for debug/testing (omits saving file on harddisk).
	 */
	
	
	/**
	 * Download the file with the hash key@param.
	 * 
	 * @param key hash of the file to download
	 * @param saveFile if true, the file will be stored in the download folder, otherwise, the file will be downloaded, but not stored. 
	 * @return the MusicFile of the downloaded file. returns null if saveFile@param == false
	 * @throws DownloadFailedException in case download fails. 
	 * @throws NotYetConnectedException 
	 */
	MusicFile download(Number160 key, boolean saveFile) throws DownloadFailedException, NotYetConnectedException;
	
	/**
	 * Search for songs by keyword. The domain enum is used to specify whether you want to perform a search for album, artist, title or genre.
	 * The keyword is a normal word, i.e. "metallica".
	 * 
	 * @param searchDomain the domain in which the search will be performed.
	 * @param keyword the keyword for lookup
	 * @return list of P2PKeywordElements with key of file and metadata belonging to the music file
	 * @throws SearchFailedException in case search fails
	 * @throws NotYetConnectedException 
	 */
	List<List<IP2PKeywordElement>> search(SearchDomain searchDomain, String keyword) throws SearchFailedException, NotYetConnectedException;
	
	/**
	 * Send a query (key) to a tracker and receive peers which claim to have the content belongig to the given query key.
	 * 
	 * @param key to query
	 * @return a collection of trackerdata objects, containing information about the peers having/responsible for the data.
	 * @throws NotYetConnectedException 
	 */
	Collection<TrackerData> queryTracker(Number160 key) throws NotYetConnectedException;
	
	
	/**
	 * adds the element to the download queue. the queue is polled from time to time and if the queue is not empty,
	 * the application will start downloading the content with the key in the IP2PKeywordElement.
	 * @param the getHash() of the element will be used for downloading the data file from the tracker.
	 * @throws NotYetConnectedException 
	 */
	boolean addToDownloadQueue(IP2PKeywordElement element) throws NotYetConnectedException;
}
