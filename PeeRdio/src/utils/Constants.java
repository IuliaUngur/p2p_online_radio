package utils;

import java.awt.Color;

public class Constants {

	public static final Color MSG_STANDARD_COLOR = Color.black;
	public static final String DOWNLOADED_FILES_FOLDER = "downloads";
	public static final String TEMPORARY_XML_FILE = "temporary_RatingsDB.xml";
	public static final String RATINGS_DB_XML_FILE = "RatingsDB.xml";
	public static final int DEFAULT_P2P_PORT = 5000;

	
	// playlist building
	public static final int MAX_PEERS_FOR_BUILDING_PLAYLIST = 10;
	public static final int MAX_TRACKS_FOR_TRACKDATABASE = 20;
	public static final int MAX_PEERS_BEST_MATCHES = 10;
	public static final int MAX_TRACKS_PER_PLAYLIST = 15;
	public static final int MIN_RATINGS_REQUIRED = 5;
	
	//ratings for the songs
	public static final int RATING_FIVE = 10;
	public static final int RATING_FOUR = 7;
	
	public static final int RATING_THREE = 5;
	public static final int RATING_TWO = 2;
	public static final int RATING_ONE = 0;

	public static final int RATING_PLAY = RATING_FOUR;
	
	//string constants to make XML handling use constant identifiers
	public static final String FILEHASH_IDENTIFIER = "FILEHASH";
	public static final String RATING_IDENTIFIER = "RATING";

}
