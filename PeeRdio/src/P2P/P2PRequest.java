package P2P;

import java.io.Serializable;

public class P2PRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Message Types - they define the protocol, i.e. what will the peer send back as a reply to a request?
	 */
	public enum P2PRequestType { 
		/* download a file by sending a key (Number160) to the peer. The peer sends back the file matching the hash */ 
		DownloadRequest, 
		/* send a list of tracks to a peer and get back correlation */
		CorrelationRequest,
		/* Request a number of randomly selected top tracks and get back a list of tracks */
		TopTracksRequest
	}
	
	private P2PRequestType type;
	private Object payload;
	
	public P2PRequest(P2PRequestType type, Object payload) {
		this.type = type;
		this.payload = payload;
	}

	public P2PRequestType getType() {
		return type;
	}

	public Object getPayload() {
		return payload;
	}
	
}
