
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.tomp2p.connection.PeerConnection;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureBootstrap; 
import net.tomp2p.futures.FutureData;
import net.tomp2p.futures.FutureTracker;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.p2p.config.ConfigurationTrackerStore;
import net.tomp2p.p2p.config.Configurations;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.TrackerData;
import net.tomp2p.utils.Utils;

public class PrototypePeer {
	
	final private Peer peer;
	final private Map<Number160, ArrayList<String>> downloaded = new HashMap<Number160, ArrayList<String>>(); // file hash , path to file => 91293, C:\song.mp3
	private int nodeId;

	
	public PrototypePeer (int nodeId) throws Exception {
		
		this.nodeId = nodeId;
		this.peer = new PeerMaker(new Number160(nodeId)).setPorts(4000+nodeId).buildAndListen();
		
		//Bootstrapping (if not Bootstrapnode)
		if(nodeId != 1){
			FutureBootstrap fb = this.peer.bootstrap(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),4001));
			fb.awaitUninterruptibly();
		}
		
		setReplyHandler();
		
	}
	
	private void setReplyHandler()
	{
		peer.setObjectDataReply(new ObjectDataReply()
		{
			@Override
			public Object reply(PeerAddress sender, Object request) throws Exception
			{
				if(request!=null && request instanceof Number160){
					//returns all values for a given key
					ArrayList<String> values = downloaded.get((Number160)request);
					return values;
				}
				else return null;
			}
		});
	}
	
	//This method is called after providing/downloading a new song. 
	public void announce(Number160 key, String Id3Tag) throws IOException
	{

			if(downloaded.containsKey(key)){
				ArrayList<String> values = (ArrayList<String>) downloaded.get(key);
				values.add(Id3Tag);
				downloaded.remove(key);
				downloaded.put(key,values);	
			}else{
				ArrayList<String> values = new ArrayList<String>();
				values.add(Id3Tag);
				downloaded.put(key, values);
			}
			
		announce();
	}
	
	private void announce() throws IOException
	{
		for(Map.Entry<Number160, ArrayList<String>> entry:downloaded.entrySet())
		{
			ConfigurationTrackerStore cts = Configurations.defaultTrackerStoreConfiguration();
			Collection<String> tmp = new ArrayList<String>();
			for(ArrayList<String> values : downloaded.values()){
				for(String value : values){
					if(!tmp.contains(value)){
						tmp.add(value);
					}
				}
			}
			
			tmp.remove(entry.getValue());
			cts.setAttachement(Utils.encodeJavaObject(tmp.toArray(new String[0])));
			peer.addToTracker(entry.getKey(), cts).awaitUninterruptibly();
		}
	}

	//This method makes a file from the provided pathname and returns it
	private File makeFile(String path){
		
		File file = new File(path);
		return file;
		
	}
	
	//Lists up every peer in the peermap of this node
	private void showNeighbours(){
		
		Collection<PeerAddress> known = this.peer.getPeerBean().getPeerMap().getAll();
		Iterator<PeerAddress> iter = known.iterator();
		System.out.println("I know: ");
		while(iter.hasNext()){
			System.out.println(iter.next().toString());
		}
		
	}
	
	public int getNodeId(){
		return nodeId;
	}

	public ArrayList<String> download(Number160 key) throws IOException, ClassNotFoundException
	{
		FutureTracker futureTracker = peer.getFromTracker(key, Configurations.defaultTrackerGetConfiguration());
		
		//now we know which peer has this data, and we also know what other things this peer has
		futureTracker.awaitUninterruptibly();
		Collection<TrackerData> trackerDatas = futureTracker.getTrackers();
		
		//here we download all files related to the key from all trackers having this key
		Iterator<TrackerData> iter = trackerDatas.iterator();
		if(!iter.hasNext())
			return null;
		
		ArrayList<String> results = new ArrayList<String>();
		while(iter.hasNext()){
			
			FutureData futureData = peer.send(iter.next().getPeerAddress(), key);
			futureData.awaitUninterruptibly();
			@SuppressWarnings("unchecked")
			//This list contains all files a peer has for a given key
			ArrayList<String> res = (ArrayList<String>)futureData.getObject();
			for(String str : res){
				results.add(str);
				// we need to announce that we have this piece now
				announce(key,str);
			}

		}
		

		return results;
	}
	
/*
	//Gives back an Iterator for all values (filepaths) for a given key
	public Iterator<Data> get(String key) throws ClassNotFoundException, IOException {
		FutureDHT futureDHT=peer.getAll(Number160.createHash(key));
		futureDHT.awaitUninterruptibly();
		if(futureDHT.isSuccess()) {
			return futureDHT.getDataMap().values().iterator();
		}
		return null;
	}
	
	//Adds a new value to a key
	public void store(String key, Object o) throws IOException {
		peer.add(Number160.createHash(key), new Data(o)).awaitUninterruptibly();
	}
*/
}
