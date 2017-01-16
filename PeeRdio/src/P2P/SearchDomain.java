package P2P;

import net.tomp2p.peers.Number160;

public enum SearchDomain {
	ARTIST("ARTIST"), 
	ALBUM("ALBUM"),
	GENRE("GENRE"),
	SONG("SONG");
	
	private String key;
	private SearchDomain(String key) {
		this.key = key;
	}
	
	public Number160 getHash() { 
		return Number160.createHash(key); 
	}
}
