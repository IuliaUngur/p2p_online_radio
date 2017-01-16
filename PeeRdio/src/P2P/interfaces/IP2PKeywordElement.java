package P2P.interfaces;

import java.io.Serializable;

import music.metadatacrawler.MusicMetaData;
import net.tomp2p.peers.Number160;

public interface IP2PKeywordElement extends Serializable {
	public MusicMetaData getMetaData();
	public Number160 getHash();
}
