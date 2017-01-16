package music.rating.client;

import music.rating.RatingsDatabase;
import music.rating.host.RatingHost;
import music.rating.interfaces.Rater;

public class RatingClient implements Rater {
	
	RatingsDatabase clientRatDB;
	RatingHost host;
	
	public RatingClient(){
		clientRatDB = new RatingsDatabase();
	}
	
	public void setHost(RatingHost host){
		this.host = host;
	}
	
	public void rateSong(String songKey, int rating){
		
		if(songRatingExists(songKey))
		{
			int newRating = clientRatDB.updateSongRating(songKey, rating);
			System.out.println("CLIENT::The song "+songKey+" was already rated once, updating the rating to "+newRating+".");
	}
		else
		{
			clientRatDB.addSongRating(songKey, rating);
			System.out.println("CLIENT::Rated the song "+songKey+" with the rating "+rating+".\n");
			
			
		}
		
		//***tell host to rate this song too***
		//TODO: TomP2P Call
		System.out.println("Calling the host of this song to update its rating...\n");
		host.rateSong(songKey, rating);
	
	}
	
	public boolean songRatingExists(String songKey){
		return (clientRatDB.getSongRating(songKey) != Integer.MIN_VALUE);
	}
	
}
