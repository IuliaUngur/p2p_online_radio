package music.rating.host;

import music.rating.RatingsDatabase;
import music.rating.interfaces.Rater;

public class RatingHost implements Rater {
	
	RatingsDatabase hostRatDB;
	
	public RatingHost(){
		 hostRatDB= new RatingsDatabase();
	}
	
	public void rateSong(String songKey, int rating){
		if(songRatingExists(songKey))
		{
			int newRating = hostRatDB.updateSongRating(songKey, rating);
			System.out.println("HOST::The song "+songKey+" was already rated once, updating the rating to "+newRating+".");
		}
		else
		{
			hostRatDB.addSongRating(songKey, rating);		
			System.out.println("HOST::Rated the song "+songKey+" with the rating "+rating+".\n");
			
		}
	}

	public boolean songRatingExists(String songKey) {
		return (hostRatDB.getSongRating(songKey) != Integer.MIN_VALUE);
	}
	
	public void addSong(String songKey)
	{
		
	}
}
