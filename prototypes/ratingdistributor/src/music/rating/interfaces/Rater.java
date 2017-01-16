package music.rating.interfaces;

public interface Rater {
	void rateSong(String songKey,int rating);
	boolean songRatingExists(String songKey);
}
