// Joshua Haynes
// Spotify music de-duplicator
// 22 October 2025
// Maven project, Java 21

package main;

import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MusicSorter {
	
	public static String authToken;
	public static String spotifyId;
	
	public final static String baseUrl = "https://api.spotify.com/v1/me";
	public final static String tracksUrl = "/tracks?limit=50&offset=";
	public final static String playlistsUrl = "/playlists?limit=50&offset=";
	
	
	public static Map<String,Track> likedSongsMap;
	public static Set<String> likedSongsIdSet;
	public static boolean listFullyPopulated = false;
	
	public static Set<Track> unplayableSongsSet;
	public static Set<Track> playableSongsSet;
	
	public static Set<Track> localSongsSet;
	
	public static Set<Track> duplicateSongsSet;
	
	public static Set<String> playlistIdsSet;
	public static boolean playListIdsPopulated = false;
	
	public static Set<Track> unlikedPlaylistSongsSet;
	
	public static final String newDuplicatePlaylistBaseName = "[MSA] Duplicates from Liked Songs";
	public static final String newUnplayablePlaylistBaseName = "[MSA] Unplayable Songs from Liked Songs";
	public static final String newLocalPlaylistBaseName = "[MSA] Local Songs from Playlists";
	public static final String newUnlikedPlaylistBaseName = "[MSA] Unliked Songs from Playlists";
	public static final int maxPlaylistSize = 9000;

	public static void main(String[] args) {
		likedSongsMap = new HashMap<String,Track>();
		likedSongsIdSet = new HashSet<String>();
		
		unplayableSongsSet = new HashSet<Track>();
		playableSongsSet = new HashSet<Track>();
		
		localSongsSet = new HashSet<Track>();
		
		duplicateSongsSet = new HashSet<Track>();
		
		playlistIdsSet = new HashSet<String>();
		unlikedPlaylistSongsSet = new HashSet<Track>();
		
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("src/application.properties"));
			authToken = prop.getProperty("authToken");
			spotifyId = prop.getProperty("spotifyId");
		} catch (Exception e) {
			System.out.println("Error reading properties: " + e.getMessage());
		}
		
		System.out.println("~ Music Sorter Alpha ~");
		
		getAllPlaylists(); // josh test

		populateLikedSongsList();
		
		System.out.println("Total tracks:");
		int playSize = playableSongsSet.size();
		int unplaySize = unplayableSongsSet.size();
		int totalSize = playSize + unplaySize;
		System.out.println(totalSize);
//		System.out.println("All playable tracks:");
//		System.out.println(playSize);
		System.out.println("Unplayable tracks:");
		System.out.println(unplaySize);
		
//		System.out.println("Total unique tracks:");
//		System.out.println(likedSongsMap.size());
		
//		System.out.println("Total track ids:");
//		System.out.println(likedSongsIdSet.size());
		
		scanListForDuplicates();
		
		System.out.println("Duplicate tracks:");
		System.out.println(duplicateSongsSet.size());
		
		
		
		System.out.println("Playlists:");
		System.out.println(playlistIdsSet.size());
		
		findUnlikedPlaylistTracks();
		
		System.out.println("Local tracks in playlists:");
		System.out.println(localSongsSet.size());
		
		System.out.println("Unliked tracks in playlists:");
		System.out.println(unlikedPlaylistSongsSet.size());
		
		// create new playlists (size max 9,000)
		addDuplicatesToPlaylist();
		addUnplayablesToPlaylist();
		addLocalPlaylistTracksToPlaylist();
		addUnlikedPlaylistTracksToPlaylist();
	}
	
	public static void populateLikedSongsList() {
		try {
			System.out.print("\nGetting liked songs");
			int offset = 0;
			while (!listFullyPopulated) {
				getLikedSongs(offset);
				offset += 50;
				if (Math.random() < .8) { System.out.print("."); } // 80% chance
				if (offset > 1000) { listFullyPopulated = true; }// josh test
			}
			System.out.println("done.\n");
			
		} catch (Exception e) {
			System.out.println("Error getting liked songs: " + e.getMessage());
		}
	}
	
	public static void getLikedSongs(int offset) throws Exception {
		URI uri = new URI(baseUrl + tracksUrl + offset);
		URL url = uri.toURL();
							
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Authorization", "Bearer " + authToken);
		conn.connect();

		int responsecode = conn.getResponseCode();
		
		if (responsecode != 200) {
		    throw new RuntimeException("HttpResponseCode: " + responsecode);
		} else {
			JSONObject likedSongsJSON = new JSONObject(new JSONTokener(conn.getInputStream())); 
			JSONArray tempLikedSongsArray = likedSongsJSON.getJSONArray("items");
						
			if (tempLikedSongsArray.length() < 1) {
				listFullyPopulated = true;
				return;
			} else {
				
				for (int i = 0; i < tempLikedSongsArray.length(); i++ ) {
					JSONObject trackJSON_ = (JSONObject) tempLikedSongsArray.get(i);
					JSONObject trackJSON = (JSONObject) trackJSON_.getJSONObject("track");
					
					Track tempTrack = new Track();
					tempTrack.setName(trackJSON.getString("name"));
					tempTrack.setTrackId(trackJSON.getString("id"));
					tempTrack.setDuration(trackJSON.getInt("duration_ms"));
					tempTrack.setPlayable(trackJSON.getBoolean("is_playable"));
					tempTrack.setLocal(trackJSON.getBoolean("is_local"));
					
					JSONArray tempArtistsArray = trackJSON.getJSONArray("artists");
					List<String> tempArtists = new ArrayList<String>();
					for (int j = 0; j < tempArtistsArray.length(); j++ ) {
						JSONObject artistJSON = (JSONObject) tempArtistsArray.getJSONObject(j);
						tempArtists.add(artistJSON.getString("name"));
					}
					tempTrack.setArtists(tempArtists);
					
					// add Track to sets
					if (tempTrack.isPlayable()) { 
						playableSongsSet.add(tempTrack);
					} else {
						unplayableSongsSet.add(tempTrack);
					}
					
					int sizeBefore = likedSongsMap.size();
					likedSongsMap.put(tempTrack.getName(), tempTrack);
					if (sizeBefore == likedSongsMap.size()) { // not added bc its a duplicate track name
						duplicateSongsSet.add(tempTrack);
						duplicateSongsSet.add(likedSongsMap.get(tempTrack.getName()));
					}
					
					likedSongsIdSet.add(tempTrack.getTrackId());
				}
			}		
		}
		return;
	}
	
	public static void scanListForDuplicates() { // O(n^2)
		System.out.print("\nScanning for duplicates");
		
		List<String> songNameList = new ArrayList<String>(likedSongsMap.keySet()); 
		
		for (int b = 0; b < songNameList.size(); b++) {
			if (Math.random() < .05) { System.out.print("."); } // 5% chance
			getTrackDuplicates(songNameList, b);
		}
		
		System.out.println("done.\n");
	}
	
	public static void getTrackDuplicates(List<String> songNameList,  int startIndex) {
		String currentTrackName = songNameList.get(startIndex);
		
		if (!currentTrackName.contains("(")) { // no features, name is normal
			return;
		}
		
		for (int c = startIndex+1; c < songNameList.size(); c++) {
			String nextTrackName = songNameList.get(c);
			if (compareWithoutFeatures(currentTrackName, nextTrackName)) {
				duplicateSongsSet.add(likedSongsMap.get(currentTrackName));
				duplicateSongsSet.add(likedSongsMap.get(nextTrackName));
			}
		}
	}
	
	public static boolean compareWithoutFeatures(String s1, String s2) {
		String split1 = s1.split("\\s\\(")[0];
		String split2 = s2.split("\\s\\(")[0];
	
		if (split1.equals(split2)) {			
			if (likedSongsMap.get(s1).getArtists().equals(likedSongsMap.get(s2).getArtists())) {
//				System.out.println("\n[" + s1 + " found to be similar to " + s2 + "]");
				return true;
			}
		}
		
		return false;
	}
	
	public static void findUnlikedPlaylistTracks()  {
		System.out.print("\nGetting unliked songs from all your playlists (excludes any playlist with EXCLUDE in the description)");
		
		try {
			for (String playlistId : playlistIdsSet) {
				getUnlikedSongsFromPlaylist(playlistId);
			}
			System.out.println("done.\n");
		
		} catch (Exception e) {
			System.out.println("Error getting songs from playlists: " + e.getMessage());
		}
	}
	
	public static void  getUnlikedSongsFromPlaylist(String playlistId) throws Exception {
		String pQuery = "https://api.spotify.com/v1/playlists/"+playlistId+"/tracks?limit=50&offset=";
		boolean playlistCompleted = false;
		int offsetC = 0;
		
		while (!playlistCompleted) {
			
			URI uri = new URI(pQuery + offsetC);
			URL url = uri.toURL();
								
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + authToken);
			conn.connect();

			int responsecode = conn.getResponseCode();
			
			if (responsecode != 200) {
			    throw new RuntimeException("HttpResponseCode: " + responsecode);
			} else {
				JSONObject playlistSongsJSON = new JSONObject(new JSONTokener(conn.getInputStream())); 
				JSONArray tempPlaylistSongsArray = playlistSongsJSON.getJSONArray("items");
							
				if (tempPlaylistSongsArray.length() < 1) {
					playlistCompleted = true;
					return;
				} else {

					for (int i = 0; i < tempPlaylistSongsArray.length(); i++ ) {
						JSONObject playSongJSON_ = (JSONObject) tempPlaylistSongsArray.get(i);
						JSONObject playSongJSON = (JSONObject) playSongJSON_.getJSONObject("track");
												
						Track tempTrackB = new Track();
						tempTrackB.setName(playSongJSON.getString("name"));
						tempTrackB.setDuration(playSongJSON.getInt("duration_ms"));
						tempTrackB.setPlayable(true); // (playSongJSON.getBoolean("is_playable"));
						tempTrackB.setLocal(playSongJSON.getBoolean("is_local"));
						if (!tempTrackB.isLocal()) { 
							tempTrackB.setTrackId(playSongJSON.getString("id")); 
						}
						
						JSONArray tempPlayArtistsArray = playSongJSON.getJSONArray("artists");
						List<String> tempPlayArtists = new ArrayList<String>();
						for (int j = 0; j < tempPlayArtistsArray.length(); j++ ) {
							JSONObject artistPlayJSON = (JSONObject) tempPlayArtistsArray.getJSONObject(j);
							tempPlayArtists.add(artistPlayJSON.getString("name"));
						}
						tempTrackB.setArtists(tempPlayArtists);
						
						
						if (tempTrackB.isLocal()) {
							localSongsSet.add(tempTrackB);
						} else if (!likedSongsIdSet.contains(tempTrackB.getTrackId())) {
//							System.out.println("[unliked song "+tempTrackB.getName()+" found in playlist "+playlistId+"]");
							unlikedPlaylistSongsSet.add(tempTrackB); 
						}
					}
				}
			}
			
			offsetC += 50;
			if (Math.random() < .25) { System.out.print("."); } // 25% chance
		}
	}
	
	public static void getAllPlaylists() {
		try {
			int offsetB = 0;
			while (!playListIdsPopulated) {
				System.out.println("Getting 50 playlists"); // josh test
				getPlaylistIds(offsetB);
				offsetB += 50;
	//			System.out.print("~");
			}
		} catch (Exception e) {
			System.out.println("Error getting playlists: " + e.getMessage());
		}
	}
	
	public static void getPlaylistIds(int offset) throws Exception {
		URI uri = new URI(baseUrl + playlistsUrl + offset);
		URL url = uri.toURL();
							
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Authorization", "Bearer " + authToken);
		conn.connect();

		int responsecode = conn.getResponseCode();
		
		if (responsecode != 200) {
		    throw new RuntimeException("HttpResponseCode: " + responsecode);
		} else {
			JSONObject playlistsJSON = new JSONObject(new JSONTokener(conn.getInputStream()));
			JSONArray tempPlaylistsArray = playlistsJSON.getJSONArray("items");
			
			if (tempPlaylistsArray.length() < 1) {
				playListIdsPopulated = true;
				return;
			} else {
				for (int c = 0; c < tempPlaylistsArray.length(); c++) {
					JSONObject playlistJSON = (JSONObject) tempPlaylistsArray.get(c);
				
					if (playlistJSON.getJSONObject("owner").getString("id").equals(spotifyId) && 
							!playlistJSON.getString("description").contains("EXCLUDE")) {
						playlistIdsSet.add(playlistJSON.getString("id"));
//						System.out.println("[playlist "+playlistJSON.getString("name")+" searched]");
					} 
				}	
			}
		}
	}
	
	public static void addDuplicatesToPlaylist() {
//		duplicateSongsSet
//		newDuplicatePlaylistBaseName // add system MS to end?
//		maxPlaylistSize
		
	}
	
	public static void addUnplayablesToPlaylist() {
//		unplayableSongsSet
//		newUnplayablePlaylistBaseName
		
	}
	
	public static void addLocalPlaylistTracksToPlaylist() {
//		localSongsSet
//		newLocalPlaylistBaseName
		
	}
	
	public static void addUnlikedPlaylistTracksToPlaylist() {
//		unlikedPlaylistSongsSet
//		newUnlikedPlaylistBaseName
		
	}
}
