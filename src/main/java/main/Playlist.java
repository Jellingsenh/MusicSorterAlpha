// Joshua Haynes
// Spotify music de-duplicator
// 22-31 October 2025
// Maven project, Java 21

package main;

import java.io.Serializable;
import java.util.List;

public class Playlist implements Serializable {
	private static final long serialVersionUID = -1576212790789637339L;
	
	private String name;
	private String id;
	private List<Track> tracks;
	
	public Playlist(String name, String id) {
		setName(name);
		setId(id);
	}
	
	public Playlist(String name, String id, List<Track> tracks) {
		setName(name);
		setId(id);
		setTracks(tracks);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public void setTracks(List<Track> allTracks) {
		this.tracks = allTracks;
	}
}
