// Joshua Haynes
// Spotify music de-duplicator
// 22-31 October 2025
// Maven project, Java 21

package main;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Scanner;

public class MusicSorter {
	private static String authToken;
	private static String spotifyId;
	
	private static MusicData musicData;
	private static ApiConnector spotifyConnector;
	
	public static void main(String[] args) {
		System.out.println("~ Music Sorter Alpha ~\n");
		
		musicData = new MusicData();
		getAuthorization();
		spotifyConnector = new ApiConnector(spotifyId, authToken);
		
		getData();
		
		processData();

		System.out.println("\n~ Fin ~");
	}
	
	// Authorization function:
	
	private static void getAuthorization() {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("src/application.properties"));
			authToken = prop.getProperty("authToken");
			spotifyId = prop.getProperty("spotifyId");
		} catch (Exception e) {
			System.out.println("Error reading properties: " + e.getMessage());
		}
	}
	
	// Data functions:
	
	private static void getData() {
		if (musicData.doesCacheExist()) {
			System.out.print("Cached data found. Retrieve it? (y/n): ");
			Scanner yesNoInput = new Scanner(System.in);
		    if (yesNoInput.next().equalsIgnoreCase("y")) {
		    	loadCachedData();
		    } else {
		    	getNewData();
		    }
		    yesNoInput.close();
		} else {
			getNewData();
		}
	}
	
	private static void loadCachedData() {
		musicData.loadData();
	}
	
	private static void getNewData() {
		spotifyConnector.getData();
		musicData.setData(spotifyConnector.getLikedSongsList(), spotifyConnector.getPopulatedlaylistsSet());
	}

	private static void processData() {
		musicData.scanForDuplicates();
		musicData.combineDuplicatesIntoPlaylistMaps();
		
		spotifyConnector.handleDuplicates(musicData.getFullDuplicateTrackIdsSet(), musicData.getPlaylistsAndSongsUrisToRemoveMap(), musicData.getBestDuplicateTrackIdsSet(), musicData.getPlaylistsAndSongUrisToAddMap());
		
		musicData.displayLocalTrackInfo();
	}
}
