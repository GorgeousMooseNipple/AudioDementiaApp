## AudioDementia - music streaming android application

Client app for [this server.](https://github.com/GorgeousMooseNipple/AudioDementiaServer)  
The coolest part of this whole app are definitely theese logos created by my friend [@ChrisNotFound](https://github.com/ChrisNotFound):  
<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/komb_1.png" width=200 vspace=10 align="left">
<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/komb_3.png" width=200 vspace=10 align="center">

## Contents
* [Description](#description)
* [Features](#features)
* [Screenshots](#screenshots)
* [Permissions](#permissions)
* [Dependencies](#dependencies)
* [Future Improvements](#future-improvements)

## Description
This application provides user interface and communicates with [server's](https://github.com/GorgeousMooseNipple/AudioDementiaServer) API to search and stream music from database across the network.

Target platform: **Android 9**; Minimal: **Android 6.0**

## Features
* Search database for songs, albums and artists;
* Stream music from remote server;
* Create custom playlists.

## Screenshots
Registration and login forms:

<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/Screenshot_2022-06-23-22-21-39-541_lab.android.audiodementia.png" width=200 hspace=5 vspace=10 align="left">
<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/Screenshot_2022-06-23-22-21-32-539_lab.android.audiodementia.png" width=200 hspace=5 vspace=10 align="center">

Searching for songs, albums and artists:

<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/Screenshot_2022-06-23-22-28-13-907_lab.android.audiodementia.png" width=200 hspace=5 vspace=10 align="left">
<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/Screenshot_2022-06-23-22-29-16-820_lab.android.audiodementia.png" width=200 hspace=5 vspace=10 align="center">

<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/Screenshot_2022-06-23-22-29-50-426_lab.android.audiodementia.png" width=200 hspace=5 vspace=10 align="center">

Music player fragment and navigation menu:

<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/Screenshot_2022-06-23-22-28-39-219_lab.android.audiodementia.png" width=200 hspace=5 vspace=10 align="left">
<img src="https://raw.githubusercontent.com/GorgeousMooseNipple/AudioDementiaApp/media/media/images/Screenshot_2022-06-23-22-28-46-918_lab.android.audiodementia.png" width=200 hspace=5 vspace=10 align="center">

## Permissions
List of permissions required by this application to fully function.
* **INTERNET** is needed for network communications with server;
* **READ/WRITE_EXTERNAL_STORAGE** for caching;
* **READ_PHONE_STATE** grants access to phone state to control playback volume in case of ongoing phone calls; 
* **WAKE_LOCK** is required by MediaPlayer to keep playing when device is locked;
* **MEDIA_CONTENT_CONTROL** used to control playback volume dependent on sounds from other applications.

## Dependencies
* [Greenrobot EventBus](https://github.com/greenrobot/EventBus) handles significant part of background work in this app, primarily used for networking;
* [Picasso](https://github.com/square/picasso) helps to asynchronously download and display album covers;
* [Gson](https://github.com/google/gson) is used to serialize and deserialize JSON data which comes from server;
* and [Mini Equalizer by Claucookie](https://github.com/claucookie/mini-equalizer-library-android) to display this cool little equalizer animation when music is being played.

## Future Improvements
* Possibility to delete songs from playlists would be nice;
* Fully implement transport controls with MediaSession;
* Check refresh token functionality.