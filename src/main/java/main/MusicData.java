// Joshua Haynes
// Spotify music de-duplicator
// 22-31 October 2025
// Maven project, Java 21

package main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MusicData {
	private Cacher cacher;
	private Set<Set<Track>> duplicateSetsSet;
	private Set<String> fullDuplicateTrackIdsSet;
	private Set<String> bestDuplicateTrackIdsSet;
	private Map<Playlist, Set<Track>> playlistsAndSongsToRemoveMap;
	private Map<Playlist, Set<Track>> playlistsAndSongsToAddMap;
	
	public MusicData() {
		cacher = new Cacher();
		duplicateSetsSet = new HashSet<Set<Track>>();
		fullDuplicateTrackIdsSet = new HashSet<String>();
		bestDuplicateTrackIdsSet = new HashSet<String>();
		playlistsAndSongsToRemoveMap = new HashMap<Playlist,Set<Track>>();
		playlistsAndSongsToAddMap = new HashMap<Playlist,Set<Track>>();
	}
	
	// caching functions:
	
	public boolean doesCacheExist() {
		return cacher.doesCacheExist();
	}
	
	public void loadData() {
		cacher.loadDataFromCache();
	}
	
	public void setData(List<Track> likedSongsList, Set<Playlist> populatedPlaylistsSet) {
		cacher.setLikedSongsList(likedSongsList);
		cacher.setPopulatedPlaylistsSet(populatedPlaylistsSet);
		
		cacher.cacheData();
	}
	
	// getting duplicates functions:
	
	public void scanForDuplicates() {
		System.out.print("\nScanning for duplicates...");
		
		scanList(cacher.getLikedSongsList());
		
		for (Playlist p : cacher.getPopulatedPlaylistsSet()) {
			scanList(p.getTracks());
		}
		
		System.out.println("done.\n");
		
		System.out.println(duplicateSetsSet.size() + " songs found with duplicates.");
	}
	
	private void scanList(List<Track> trackList) {
		for (int b = 0; b < trackList.size(); b++) {
			if (!alreadyInDuplicatesList(trackList.get(b))){
				Set<Track> bTrackSet = getTrackDuplicates(trackList, b); 
				if (bTrackSet.size() > 0) {
					duplicateSetsSet.add(bTrackSet);
				} 
				if (Math.random() < .01) { System.out.print("."); } // 1% chance
			}
		}
	}
	
	private boolean alreadyInDuplicatesList(Track track) {
		for (Set<Track> trackList : duplicateSetsSet) {
			for (Track t0 : trackList) {
				if (compareWithoutFeatures(t0, track)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean compareWithoutFeatures(Track track1, Track track2) {
		if (
			track1.equals(track2) || 
			track1.getUri().equals(track2.getUri()) || 
			track1.getId().equals(track2.getId()) ||
			(track1.getName().split("\\s\\(")[0].equals(track2.getName().split("\\s\\(")[0]) && track1.getArtists().equals(track2.getArtists())) 
		) {
//			System.out.println("\n[" + track1.getName() + " found to be similar to " + track2.getName() + "]");
			return true;
		}
		return false;
	}
	
	private Set<Track> getTrackDuplicates(List<Track> songList,  int startIndex) {
		Track currentTrack = songList.get(startIndex);
		
		Set<Track> similarTrackSet = new HashSet<Track>();
		
		for (int c = startIndex+1; c < songList.size(); c++) {
			Track nextTrack = songList.get(c);
			if (compareWithoutFeatures(currentTrack, nextTrack)) {
				similarTrackSet.add(nextTrack);
			}
		}
		
		if (similarTrackSet.size() > 0) {
			similarTrackSet.add(currentTrack);
		}
		
		return similarTrackSet;
	}
	
	public void combineDuplicatesIntoPlaylistMaps() {
		System.out.print("\nCombining duplicates...");
		for (Set<Track> similarTracksSet : duplicateSetsSet) {
			combineListOfDuplicates(similarTracksSet);
			for (Track t: similarTracksSet) {
				fullDuplicateTrackIdsSet.add(t.getId());
			}
			if (Math.random() < .25) { System.out.print("."); } // 25% chance
		}
	}
	
	private void combineListOfDuplicates(Set<Track> similarTracks) {		
		String bestTrackUri = getBestTrackUri(similarTracks);
	
		if (bestTrackUri.equals("none")) {
			System.out.println("No best track found.");
		} else {
			for (Track similarTrack: similarTracks) {
				boolean isBest = similarTrack.getUri().equals(bestTrackUri);
				
				if (isBest) {
					bestDuplicateTrackIdsSet.add(similarTrack.getId());
				}
				
				for (Playlist P : getAllPlaylistsForTrack(similarTrack)) {
					addToRemoveMap(P, similarTrack);
					
					if (isBest) {
						addToAddMap(P, similarTrack);
					}
				}
			}
		}
	}
	
	private String getBestTrackUri(Set<Track> similarTracks) {		
		String bestTrackUri = "none";
		int currentAlbumSize = 0;
		String currentAlbumReleaseDate = "1900-01-01";
		int currentDuration = 0;
		
		for (Track similarTrack: similarTracks) { // find best track
			if (similarTrack.getAlbumTracks() > currentAlbumSize || 
			similarTrack.getAlbumTracks() == currentAlbumSize && timeBetweenDates(similarTrack.getAlbumReleaseDate(),currentAlbumReleaseDate) > 0 ||
			similarTrack.getAlbumTracks() == currentAlbumSize && timeBetweenDates(similarTrack.getAlbumReleaseDate(),currentAlbumReleaseDate) == 0 && similarTrack.getDuration() > currentDuration) {
				bestTrackUri = similarTrack.getUri();
				currentAlbumSize = similarTrack.getAlbumTracks();
				currentAlbumReleaseDate = similarTrack.getAlbumReleaseDate();
				currentDuration = similarTrack.getDuration();
			}
		}
		return bestTrackUri;
	}
	
	private int timeBetweenDates(String albumReleaseDate, String currentAlbumReleaseDate) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        LocalDate dateA = LocalDate.parse(albumReleaseDate, formatter);
	        LocalDate dateB = LocalDate.parse(currentAlbumReleaseDate, formatter);
	        
	        if (dateA.isAfter(dateB)) {
	    			return 1;
	        }
	        
	        if (dateA.isBefore(dateB)) {
				return -1;
	        }
		} catch (Exception e) {
			return 0;
		}
		
		return 0;
	}
	
	private Set<Playlist> getAllPlaylistsForTrack(Track similarTrack) {
		Set<Playlist> trackPlaylists = new HashSet<Playlist>();
		
		for (Playlist p : cacher.getPopulatedPlaylistsSet()) {
			if (doesPlaylistContainTrackByUri(p,similarTrack)) {
				trackPlaylists.add(p);
			}
		}		
		return trackPlaylists;
	}
	
	private boolean doesPlaylistContainTrackByUri(Playlist P, Track similarTrack) {
		for (Track t : P.getTracks()) { 
			if (t.getUri().equals(similarTrack.getUri())) {
				return true;
			}
		}
		return false;
	}
	
	private void addToAddMap(Playlist P, Track T) {
		Set<Track> s = new HashSet<Track>();
		if (playlistsAndSongsToAddMap.containsKey(P)) {
			s.addAll(playlistsAndSongsToAddMap.get(P));
		}
		s.add(T);
		playlistsAndSongsToAddMap.put(P, s);
	}
	
	private void addToRemoveMap(Playlist P, Track T) {
		Set<Track> s = new HashSet<Track>();
		if (playlistsAndSongsToRemoveMap.containsKey(P)) {
			s.addAll(playlistsAndSongsToRemoveMap.get(P));
		}
		s.add(T);
		playlistsAndSongsToRemoveMap.put(P, s);
	}
	
	// display local track info function:
	
	public void displayLocalTrackInfo() {
		System.out.println("\nLocal tracks (must be added to playlists manually):");
		 
		for (Playlist playlist_ : cacher.getPopulatedPlaylistsSet()) {
			String pString = "";
			List<Track> trackSet = playlist_.getTracks();
			if (trackSet.size() > 0) {
				pString += "\n  ======>  Playlist: " + playlist_.getName() + "  <======\n{";
				int count = 0;
				for (Track track : trackSet) {
					if (track.isLocal()) {
						count += 1;
						pString += "\n    " + track.getName();
					}
				}
				if (count > 0) {
					System.out.println(pString + "\n}");
				}
			}
	    }
	}
	
	// getters & setters

	public Set<String> getFullDuplicateTrackIdsSet() {
		return fullDuplicateTrackIdsSet;
	}
	public Set<String> getBestDuplicateTrackIdsSet() {
		return bestDuplicateTrackIdsSet;
	}
	public Map<Playlist, Set<Track>> getPlaylistsAndSongsUrisToRemoveMap() {
		return playlistsAndSongsToRemoveMap;
	}
	public Map<Playlist, Set<Track>> getPlaylistsAndSongUrisToAddMap() {
		return playlistsAndSongsToAddMap;
	}
}
