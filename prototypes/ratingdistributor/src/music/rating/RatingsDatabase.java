package music.rating;

import java.util.Vector;

/**
 * 
 * Simulate a database
 *
 */
public class RatingsDatabase {
	Vector<RatingDBEntry> ratingEntries;
	
	public RatingsDatabase(){
		ratingEntries = new Vector<RatingDBEntry>();
		
	}
	
	
	public void addSongRating(String songKey,int rating)
	{
		ratingEntries.add(new RatingDBEntry(songKey, rating));
	}
	
	public int updateSongRating(String songKey,int rating)
	{

		for(int i = 0; i < ratingEntries.size();i++)
		{
			if(ratingEntries.get(i).getSongKey().equals(songKey))
			{
				ratingEntries.get(i).rating += rating;
				return ratingEntries.get(i).rating;
			}
		}
		return Integer.MIN_VALUE;
	}
	
	public int getSongRating(String songKey){
		for(int i = 0; i < ratingEntries.size();i++)
		{
			if(ratingEntries.get(i).getSongKey().equals(songKey))
			{
				return ratingEntries.get(i).getRating();
			}
		}
		//if a song is that badly rated, we can consider it as not existing
		//and create a new entry
		return Integer.MIN_VALUE;
	}
}
