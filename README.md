# MusicSorterAlpha

A music sorter, to get rid of duplicates in Spotify and find unliked songs in your playlists.

------

You must generate an auth token by following the instructions here:

[Get Auth code & token] (https://developer.spotify.com/documentation/web-api/tutorials/code-flow)

Don't forget to include `scope=user-library-read,user-library-modify,playlist-modify-public,playlist-modify-private` in the parameters for the auth code request.

------

Add your auth token to the application.properties file.

Also add your Spotify user id to the application.properties file.
