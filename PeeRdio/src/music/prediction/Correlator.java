package music.prediction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import core.MusicFile;

import net.tomp2p.peers.Number160;
import P2P.interfaces.IP2PKeywordElement;

public class Correlator {
	float correlation;
	Vector<MusicFile> spares;
	Map<Number160, MusicFile> referenceDatabase;
	IP2PKeywordElement[] requestorDatabase;

	public Correlator(HashMap<Number160, MusicFile> referenceDatabase,
			IP2PKeywordElement[] requestorDatabase) {
		//TODO: There should be no non rated songs inside of any of the datasets
		//Otherwise it will consider them as the being rated the same (-1 -(-1)) = 0 => datasets correlate
		this.referenceDatabase = referenceDatabase;
		this.requestorDatabase = requestorDatabase;
	}

	public void calculateCorrelation() {

		spares = new Vector<MusicFile>();
		correlation = 0;
		

		// The lowest of the two lengths.
		int minLength = referenceDatabase.size() < requestorDatabase.length ? referenceDatabase
				.size() : requestorDatabase.length;
		if (minLength == 0)
			return;

		float[] intersect0 = new float[minLength];
		float[] intersect1 = new float[minLength];
		int intersectLength = 0;
		
	    float total0 = 0;
	    float total1 = 0;
		for (int i = 0; i < requestorDatabase.length; i++) {
			MusicFile referenceTrack = referenceDatabase
					.get(requestorDatabase[i].getHash());
			if (referenceTrack.getMetaData().isRated()) {
				if (referenceTrack != null
						&& referenceTrack.getMetaData().isRated()) {

					// Add this to the intersect list
					intersect0[intersectLength] = requestorDatabase[i]
							.getMetaData().getRating();
					intersect1[intersectLength] = referenceTrack.getMetaData()
							.getRating();
					intersectLength++;
		            total0 += requestorDatabase[i]
							.getMetaData().getRating();
		            total1 += referenceTrack.getMetaData()
							.getRating();
					referenceDatabase.remove(requestorDatabase[i].getHash());
				}
			}
		}

		// Now that we have the totals we can calculate the actual correlation.
		float sum = 0;
		// float offset0 = total0 / intersectLength;
		// float offset1 = total1 / intersectLength;
		final float offset0 = 5;
		final float offset1 = 5;
		for (int i = 0; i < intersectLength; i++)
			sum += (intersect0[i] - offset0) * (intersect1[i] - offset1);
		
		// correlation = sum * intersectLength * intersectLength / (total0 * total1);


		// Dividing by the intersectLength would give the average track
		// correlation.
		correlation = sum / intersectLength;

		// Weight it a little towards large intersections
		if (intersectLength >= 20)
			correlation *= 2;

		// Cube it to make it weight highly towards good and bad correlations.
		correlation = correlation * correlation * correlation;

		// Add the remaining tracks that the requester never listened to to the
		// spares.
		Set<Number160> set = referenceDatabase.keySet();
		for (Number160 index : set) {
			spares.add(referenceDatabase.get(index));
		}

	}

	public float getCorrelation() {
		return correlation;
	}

	public Vector<MusicFile> getSpares() {
		return spares;
	}
}
