package music.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import utils.Constants;

import core.MusicFile;

import nanoxml.XMLElement;
import net.tomp2p.peers.Number160;

/**
 * 
 * Database to save the music file entries.
 * 
 * 
 */
public class RatingProfileHandler {

	// xml database and file
	XMLElement xmlTrackDatabase;
	private File databaseFile;
	HashMap<String, Float> ratingValues;

	/**
	 * Constructor of the rating profile handler
	 */
	public RatingProfileHandler() {
		initializeDatabase();
	}
	
	/**
	 * Initialize a new xml database
	 */
	private void initializeDatabase() {
		// initialize xml database
		xmlTrackDatabase = new XMLElement(new Hashtable<String, Float>(),
				false, false);
		xmlTrackDatabase.setName(RatingProfileHandler.class.getName());
	}

	/**
	 * Load the xml database from the database file.
	 * 
	 * @param databaseFile
	 *            The file of the database.
	 * @throws IOException
	 */
	public void load(File databaseFile) throws IOException {

		this.databaseFile = databaseFile;
		InputStream is = new FileInputStream(databaseFile);

		// parse the database file
		xmlTrackDatabase.parseFromReader(new InputStreamReader(is));

		// if its actually the right file
		if (xmlTrackDatabase.getName().equals(RatingProfileHandler.class.getName())) {

			// get the entries from file
			Enumeration e = xmlTrackDatabase.enumerateChildren();

			while (e.hasMoreElements()) {
				XMLElement xmlElement = (XMLElement) e.nextElement();

				// if it is not a track entry, skip it
				if (!xmlElement.getName().equals(MusicFile.class.getName())) {
					continue;
				}
				// add the track to the structure
				ratingValues.put((String)xmlElement.getAttribute(Constants.FILEHASH_IDENTIFIER),(Float)xmlElement.getAttribute(Constants.RATING_IDENTIFIER));
			}
		}
		is.close();
	}

	public void save(HashMap<Number160, MusicFile> musicDatabase) throws IOException {
		Set<Number160> set = musicDatabase.keySet();
		for (Number160 index : set) {
			xmlTrackDatabase.addChild(musicDatabase.get(index).getXMLElement());
		}

		File temporaryFile = new File(new File("").getAbsolutePath(), Constants.TEMPORARY_XML_FILE);
		databaseFile = new File(new File("").getAbsolutePath(),Constants.RATINGS_DB_XML_FILE);
		FileWriter fileWriter = null;
		
		try {
			fileWriter = new FileWriter(temporaryFile);
			// write header
			fileWriter.write("<?xml version=\"1.0\"?>\n");

			// write the database to file
			fileWriter.write(xmlTrackDatabase.toString());
			fileWriter.close();
			fileWriter = null;

			// Rename the temporary file to the real name of the rating db file. 
			//This makes the writing of the new file effectively atomic.
			if (!temporaryFile.renameTo(databaseFile)) {

				System.out.println("failed at renaming " + temporaryFile
						+ " to " + databaseFile + "\nAttempting to delete"
						+ databaseFile + "...\n");
				
				// we try if we can first delete the file and then rename the temp file
				if (!databaseFile.delete()) {
					throw new IOException("Failed to delete " + databaseFile + "\n");
				}

				if (!temporaryFile.renameTo(databaseFile)) {
					throw new IOException("Failed to rename " + temporaryFile
							+ " to " + databaseFile);
				}
			}
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	float getRatingOf(String hash)
	{
		//if the file already has a rating, return it, else return -1
		if(ratingValues.containsKey(hash))
		{
			return ratingValues.get(hash);
		}
		else
		{
			return -1;
		}
	}
}
