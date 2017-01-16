package music.rating;

public class RatingDBEntry {
	
	String songKey;
	int rating;
	
	public RatingDBEntry(String songKey,int rating)
	{
		this.songKey = songKey;
		this.rating = rating;
	}

	public String getSongKey() {
		return songKey;
	}

	public void setSongKey(String songKey) {
		this.songKey = songKey;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}
	
	

}
