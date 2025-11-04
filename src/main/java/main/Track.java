// Joshua Haynes
// Spotify music de-duplicator
// 22-31 October 2025
// Maven project, Java 21

package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Track implements Serializable {
	private static final long serialVersionUID = 4144541409295680478L;
	
	private String name;
	private String uri;
	private String id;
	private boolean isLocal;
	private boolean isPlayable;
	private int duration;
	private List<String> artists;
	private int albumTracks;
	private String albumReleaseDate;
	
	public Track() {
		artists = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getArtists() {
		return artists;
	}

	public void setArtists(List<String> artists) {
		this.artists = artists;
	}

	public boolean isPlayable() {
		return isPlayable;
	}

	public void setPlayable(boolean isPlayable) {
		this.isPlayable = isPlayable;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getAlbumTracks() {
		return albumTracks;
	}

	public void setAlbumTracks(int albumTracks) {
		this.albumTracks = albumTracks;
	}

	public String getAlbumReleaseDate() {
		return albumReleaseDate;
	}

	public void setAlbumReleaseDate(String albumReleaseDate) {
		this.albumReleaseDate = albumReleaseDate;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
