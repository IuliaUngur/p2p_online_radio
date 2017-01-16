import java.io.IOException;
import java.util.Iterator;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public interface IPrototypePeer {
	
	public int getNodeId();
	public void announce(Number160 key, String Id3Tag) throws IOException;
	public String download(Number160 key) throws IOException, ClassNotFoundException;

}
