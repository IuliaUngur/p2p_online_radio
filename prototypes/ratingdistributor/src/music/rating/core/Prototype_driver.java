package music.rating.core;

import music.rating.client.RatingClient;
import music.rating.host.RatingHost;

public class Prototype_driver {
	public static void main(String[] args) {
		RatingClient client = new RatingClient();
		RatingHost host = new RatingHost();
		
		client.setHost(host);
		
		if (args.length % 2 != 0) {
			System.out.println("Your arguments are not always pairs of one song with a rating. Skipping args.\n");
		} else {

		}
		// read the args to read a simple string of song names and ratings from
		// command line
		for (int i = 0; i < args.length; i += 2) {
			String key = args[i]; 	int value = Integer.parseInt(args[i+1]);
			
			//rate the song
			client.rateSong(key,value);
		}
	}
}
