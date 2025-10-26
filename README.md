# MusicSorterAlpha

A music sorter, to get rid of duplicates in Spotify and find unliked songs in your playlists.

------

You must generate an auth token by following the instructions here:

[Get Auth code & token] (https://developer.spotify.com/documentation/web-api/tutorials/code-flow)

Don't forget to include scope=user-library-read,playlist-modify-public,playlist-modify-private in the parameters for the auth code request.

------

Add your auth token to the application.properties file.

Also add your Spotify user id to the application.properties file.

------

Modify these booleans to modify the usability of the application:

`boolean askToUseCaches = true; // if false, automatically uses them
boolean logging = false; // show log statements
boolean askToMakePlaylists = true; // if false, automatically creates them`