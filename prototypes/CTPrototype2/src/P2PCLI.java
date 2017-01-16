import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import net.tomp2p.connection.PeerConnection;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class P2PCLI {
	
	PrototypePeer peer;
	
	
	public P2PCLI(int nodeId){
		
		try {
			peer = new PrototypePeer(nodeId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		provideOrSearch();
	}
	
	
	private void provideOrSearch() {
		// TODO Auto-generated method stub
		Scanner input=new Scanner(System.in);
		
		while(true){
			
			System.out.println("Type in 'provide_data' to provide new data");
			System.out.println("Type in 'search_data' to search for data with some key");
			
			String text = input.nextLine();
			
			if(text.equalsIgnoreCase("provide_data")){
				
				System.out.println("Type in a pseudo Id3-Tag of the file you want to provide");
				String id3Tag = input.nextLine();
				System.out.println("Type in some keys to reach your data separated by a whitespace");
				String[] keys = input.nextLine().split(" ");
				MessageObject o = new MessageObject(peer.getNodeId(),id3Tag);
				for(String key : keys){
					
					try {
					
						peer.announce(Number160.createHash(key), id3Tag);

						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else if(text.equalsIgnoreCase("search_data")){
				
				System.out.println("Type in some key(s) you want to search for");
				String[] keys = input.nextLine().split(" ");
				for(String key : keys){
					
					try {
						//So far, for every search-key, all files related to that key get downloaded
						ArrayList<String> res = peer.download(Number160.createHash(key));
						if(res == null){
							System.out.println("no songs found for given keywords!");
						}else{
							
							for(String str : res)
								System.out.println(str);
						}
					/*	
					if(iter != null){
						while(iter.hasNext()){
							MessageObject o = (MessageObject)iter.next().getObject();
							Number160 peeraddress = Number160.createHash(o.getId());
							//PeerConnection connection = this.peer.createPeerConnection(id, 500);
							FutureDHT fd = this.peer.send(peeraddress,o.getPath());
							fd.awaitUninterruptibly();
							String result = fd.getObject().toString();
							System.out.println(result);

						
						}
					}else{
						System.out.println("Sorry, no entrys for this key");
					}
				*/		
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
			
		}
		
	}

}
