// Joshua Haynes
// Spotify music de-duplicator
// 22-31 October 2025
// Maven project, Java 21

package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cacher {	
	private final String likedSongsFileName = "src/cache/likedSongs.txt";
	private final String populatedPlaylistsFileName = "src/cache/populatedPlaylists.txt";
	
	private List<Track> likedSongsList;
	private Set<Playlist> populatedPlaylistsSet;

	public Cacher() {
		likedSongsList = new ArrayList<Track>();
		populatedPlaylistsSet = new HashSet<Playlist>();
	}
	
	// primary caching functions
	
	public boolean doesCacheExist() {
		File f = new File(likedSongsFileName);
		File f2 = new File(populatedPlaylistsFileName);
		if(f.exists() && !f.isDirectory() && f2.exists() && !f2.isDirectory()) { 
		    return true;
		} else {
			return false;
		}
	}
	
	public void cacheData() {
		ObjectOutputStream oos = null;
		ObjectOutputStream oos2 = null;

		System.out.println("\nCaching...");
		
		try{
		    oos = new ObjectOutputStream(new FileOutputStream(likedSongsFileName));
		    oos.writeObject(likedSongsList);
		    System.out.println(likedSongsList.size() + " songs cached.");
		    
		    oos2 = new ObjectOutputStream(new FileOutputStream(populatedPlaylistsFileName));
		    oos2.writeObject(populatedPlaylistsSet);
		    System.out.println(populatedPlaylistsSet.size() + " playlists cached.");
		} catch (Exception ex) {
			System.out.println("Error caching data: " + ex.getMessage());
		} finally {
		    if (oos != null) { try { oos.close(); } catch (Exception e) { e.printStackTrace(); } }
		    if (oos2 != null) { try { oos2.close(); } catch (Exception e) { e.printStackTrace(); } }
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadDataFromCache() {
		ObjectInputStream objectinputstream = null;
		ObjectInputStream objectinputstream2 = null;
		
		System.out.println("\nRetrieving from cache...");
		
		try {
		    objectinputstream = new ObjectInputStream(new FileInputStream(likedSongsFileName));
		    likedSongsList = (List<Track>) objectinputstream.readObject();
		    System.out.println("Loaded " + likedSongsList.size() + " songs from the cache."); 
		    
		    objectinputstream2 = new ObjectInputStream(new FileInputStream(populatedPlaylistsFileName));
		    populatedPlaylistsSet = (Set<Playlist>) objectinputstream2.readObject();
		    System.out.println("Loaded " + populatedPlaylistsSet.size() + " playlists from the cache."); 
		} catch (Exception e) {
			System.out.println("Error loading playlists from cache: " + e.getMessage());
		} finally {
		    if (objectinputstream != null) { try { objectinputstream.close(); } catch (IOException e) { e.printStackTrace(); } }
		    if (objectinputstream2 != null) { try { objectinputstream2.close(); } catch (IOException e) { e.printStackTrace(); } }
		}
	}
	
	// getters & setters

	public List<Track> getLikedSongsList() {
		return likedSongsList;
	}
	
	public void setLikedSongsList(List<Track> likedSongsList) {
		this.likedSongsList = likedSongsList;
	}

	public Set<Playlist> getPopulatedPlaylistsSet() {
		return populatedPlaylistsSet;
	}
	
	public void setPopulatedPlaylistsSet(Set<Playlist> populatedPlaylistsSet) {
		this.populatedPlaylistsSet = populatedPlaylistsSet;
	}
}
