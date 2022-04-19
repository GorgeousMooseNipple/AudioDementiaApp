package lab.android.audiodementia.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Base64;

import lab.android.audiodementia.background.*;
import lab.android.audiodementia.model.Album;
import lab.android.audiodementia.model.Genre;
import lab.android.audiodementia.model.Playlist;
import lab.android.audiodementia.model.Song;

public class RestClient {

    private static final String BASE_URL = "http://192.168.43.249:5000/api/public";

    private static URL urlWithParams(String base, Map<String, String> params) throws
            MalformedURLException, UnsupportedEncodingException
    {
        if(params != null && !params.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("?");
            for (Map.Entry<String, String> param : params.entrySet()) {
                String param_name = URLEncoder.encode(param.getKey(), "UTF-8");
                sb.append(param_name);
                sb.append("=");
                String param_val = URLEncoder.encode(param.getValue(), "UTF-8");
                sb.append(param_val);
                sb.append("&");
            }
            // Delete trailing &
            sb.deleteCharAt(sb.length() - 1);
            return new URL(base + sb.toString());
        } else {
            return new URL(base);
        }
    }

    public static ConnectionCheckEvent checkConnection(int timeout) {
        try{
            URL url = new URL(BASE_URL + "/media/genres/top");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return new ConnectionCheckEvent(true, "Connected");
        } catch (Exception e) {
            return new ConnectionCheckEvent(false, "Can't connect");
        }
    }

    private static JSONObject requestWithData(String method, URL url, Map<String, String> params) throws
            IOException, JSONException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");

        return requestWithDataInner(connection, params);
    }

    private static JSONObject requestWithDataAndHeaders(String method, URL url, Map<String, String> params, Map<String, String> headers) throws
            IOException, JSONException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        for(Map.Entry<String, String> h: headers.entrySet()) {
            connection.setRequestProperty(h.getKey(), h.getValue());
        }

        return requestWithDataInner(connection, params);
    }

    private static JSONObject requestWithDataInner(HttpURLConnection connection, Map<String, String> params) throws
            IOException, JSONException
    {
        JSONObject json = new JSONObject();

        for(Map.Entry<String, String> p : params.entrySet()) {
            json.put(p.getKey(), p.getValue());
        }

        String jsonStr = json.toString();

        try (OutputStream out = connection.getOutputStream()) {
            out.write(jsonStr.getBytes(StandardCharsets.UTF_8));
        }

        connection.connect();
        int status = connection.getResponseCode();

        InputStreamReader input;
        if (status == HttpURLConnection.HTTP_OK)
            input = new InputStreamReader(connection.getInputStream());
        else
            input = new InputStreamReader(connection.getErrorStream());


        try (BufferedReader reader = new BufferedReader(input)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject response = new JSONObject(sb.toString());
            response.put("status_code", status);
            return response;
        }
    }

    private static JSONObject getRequest(
            String endpoint, Map<String, String> params, boolean useCache
    ) throws JSONException {
        try {

            URL url = urlWithParams(BASE_URL + endpoint, params);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (useCache) {
                connection.setUseCaches(true);
            }

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            return getRequestInner(connection, useCache);
        }
        catch (Exception ex) {
            JSONObject response = new JSONObject();
            response.put("exception", true);
            response.put("message", ex.getMessage());
            return response;
        }
    }

    private static JSONObject getRequestWithHeaders(
            String endpoint, Map<String, String> params, Map<String, String> headers, boolean useCache
    ) throws JSONException {
        try {

            URL url = urlWithParams(BASE_URL + endpoint, params);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (useCache) {
                connection.setUseCaches(true);
            }

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            for(Map.Entry<String, String> h : headers.entrySet()) {
                connection.setRequestProperty(h.getKey(), h.getValue());
            }
            return getRequestInner(connection, useCache);
        }
        catch (Exception ex) {
            JSONObject response = new JSONObject();
            response.put("exception", true);
            response.put("message", ex.getMessage());
            return response;
        }
    }

    private static JSONObject getRequestInner(
            HttpURLConnection connection, boolean useCache
    ) throws JSONException {
        try {

            connection.connect();

            int status = connection.getResponseCode();

            BufferedReader br;
            if (status == HttpURLConnection.HTTP_OK) {
                br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            JSONObject response = new JSONObject(sb.toString());
            response.put("status_code", status);
            response.put("exception", false);
            return response;
        }
        catch (Exception ex) {
            JSONObject response = new JSONObject();
            response.put("exception", true);
            response.put("message", ex.getMessage());
            response.put("status_code", 500);
            return response;
        }
    }

    public static UserSignInEvent signIn(String username, String pass) {
        Map<String, String> params = new HashMap<String, String>();
        try {
            String url = "/auth/token";
            Map<String, String> headers = new HashMap<String, String>();
            String auth = String.format("%s:%s", username, pass);
            auth = Base64.encodeToString(auth.getBytes(), Base64.DEFAULT);
            headers.put("Authorization", String.format("Basic %s", auth));
            JSONObject response = getRequestWithHeaders(url, params, headers, false);


            String message = response.getString("message");
            if (response.getInt("status_code") == HttpURLConnection.HTTP_OK) {
                long id = response.getLong("id");
                String token = response.getString("access_token");
                String refresh = response.getString("refresh_token");
                return new UserSignInEvent(true, message, id, username, pass, token, refresh);
            } else {
                return new UserSignInEvent(false, message);
            }
        }
        catch (Exception e) {
            return new UserSignInEvent(false, e.getMessage());
        }
    }

    public static UserRevokeTokenEvent signOut(Map<String, String> params) {
        try {
            URL url = new URL(BASE_URL + "/auth/token/revoke");
            JSONObject response = requestWithData("POST", url, params);

            String message = response.getString("message");
            if (response.getInt("status_code") == HttpURLConnection.HTTP_OK) {
                return new UserRevokeTokenEvent(true, message);
            } else {
                return new UserRevokeTokenEvent(false, message);
            }
        }
        catch (Exception e) {
            return new UserRevokeTokenEvent(false, e.getMessage());
        }
    }

    public static HttpResponse register(Map<String, String> params) {
        try {
            URL url = new URL(BASE_URL + "/auth/register");
            JSONObject response = requestWithData("POST", url, params);

            String message = response.getString("message");
            if (response.getInt("status_code") == HttpURLConnection.HTTP_OK) {
                return new HttpResponse(true, message);
            } else {
                return new HttpResponse(false, message);
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    public static HttpResponseWithData<List<Genre>> getGenres() {
        try {
            JSONObject response = getRequest("/media/genres/top", null, true);
            String message = response.getString("message");

            if (response.getInt("status_code") == HttpURLConnection.HTTP_OK) {
                String ar = response.getString("genres");
                JsonArray genresList = new JsonParser().parse(ar).getAsJsonArray();

                ArrayList<Genre> genres = new ArrayList<>();

                for (JsonElement el : genresList) {
                    JsonObject o = el.getAsJsonObject();
                    long id = o.get("id").getAsLong();
                    String title = o.get("title").getAsString();
                    genres.add(new Genre(id, title));
                }

                return new HttpResponseWithData<>(true, message, genres);
            } else {
                return new HttpResponseWithData<>(false, message, null);
            }
        }
        catch (JSONException e) {
            return null;
        }
    }

    public static HttpResponseWithData<List<Playlist>> getUserPlaylists(Map<String, String> params) {
        try {
            String token = params.remove("token");
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", String.format("Bearer %s", token));
            JSONObject response = getRequestWithHeaders("/media/playlists/user", params, headers, true);
            String message = response.getString("message");

            if (response.getInt("status_code") == HttpURLConnection.HTTP_OK) {
                if (response.has("playlists")) {
                    String ar = response.getString("playlists");
                    JsonArray playlistsList = new JsonParser().parse(ar).getAsJsonArray();

                    ArrayList<Playlist> playlists = new ArrayList<>();

                    for (JsonElement el : playlistsList) {
                        JsonObject o = el.getAsJsonObject();
                        long id = o.get("id").getAsLong();
                        String title = o.get("title").getAsString();
                        long songCount = o.get("song_count").getAsLong();
                        playlists.add(new Playlist(id, title, songCount));
                    }

                    return new HttpResponseWithData<>(true, message, playlists);
                } else
                    return new HttpResponseWithData<>(true, message, new ArrayList<>());
            } else {
                return new HttpResponseWithData<>(false, message, null);
            }
        }
        catch (JSONException e) {
            return null;
        }
    }

    public static HttpResponseWithData<List<Album>> getAlbumsByTitle(Map<String, String> params) {

        try {
            JSONObject response = getRequest("/media/albums/search", params, false);

            String message = response.getString("message");

            if (response.getInt("status_code") == HttpURLConnection.HTTP_OK) {
                if (response.has("albums")) {
                    JSONArray ar = response.getJSONArray("albums");

                    ArrayList<Album> albums = new ArrayList<>();

                    for (int i = 0; i < ar.length(); ++i) {
                        JSONObject o = ar.getJSONObject(i);
                        long id = o.getLong("id");
                        String albumTitle = o.getString("title");
                        JSONArray artists = o.getJSONArray("artists");
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < artists.length(); ++j) {
                            sb.append(artists.get(j).toString());
                            sb.append(" ");
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        String artistName = sb.toString();
                        String coverSmall = o.getString("cover_small");
                        String coverMedium = o.getString("cover_medium");
                        albums.add(new Album(id, albumTitle, artistName, coverSmall, coverMedium));
                    }

                    return new HttpResponseWithData<>(true, message, albums);
                } else
                    return new HttpResponseWithData<>(true, message, new ArrayList<>());
            } else {
                return new HttpResponseWithData<>(false, message, null);
            }
        }
        catch (JSONException e) {
            return null;
        }
    }

    public static NewPlaylistAddedEvent addNewPlaylist(String playlistTitle, long userId, String userToken) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("title", playlistTitle);
        params.put("user_id", String.valueOf(userId));
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", String.format("Bearer %s", userToken));
        try {
            URL url = new URL(BASE_URL + "/media/playlists/add");
            JSONObject response = requestWithDataAndHeaders("PUT", url, params, headers);

            try {
                String message = response.getString("message");
                if (response.getInt("status_code") == HttpURLConnection.HTTP_OK) {
                    long id = response.getLong("id");
                    String title = response.getString("title");
                    long trackCount = response.getLong("song_count");
                    Playlist newPlaylist = new Playlist(id, title, trackCount);
                    return new NewPlaylistAddedEvent(true, message, newPlaylist);
                } else {
                    return new NewPlaylistAddedEvent(false, message, null);
                }
            } catch (Exception ex) {
                return new NewPlaylistAddedEvent(false, "Exception", null);
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    public static HttpResponse addSongToPlaylist(long songId, long playlistId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("song_id", String.valueOf(songId));
        params.put("playlist_id", String.valueOf(playlistId));
        try {
            URL url = new URL(BASE_URL + "/media/playlists/add/song");
            JSONObject response = requestWithData("PUT", url, params);

            String message = response.getString("message");
            return new HttpResponse(true, message);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static HttpResponseWithData<List<Song>> getSongs(
            String url, Map<String, String> params
    ) {

        try {
            JSONObject response = getRequest(url, params, false);

            String message = response.getString("message");

            if (response.getInt("status_code") == HttpURLConnection.HTTP_OK) {
                if (response.has("songs")) {
                    JSONArray ar = response.getJSONArray("songs");

                    ArrayList<Song> songs = new ArrayList<>();

                    for (int i = 0; i < ar.length(); ++i) {
                        JSONObject o = ar.getJSONObject(i);
                        long id = o.getLong("id");
                        String songTitle = o.getString("title");
                        long duration = o.getLong("duration");
                        String albumTitle = o.getString("album");
                        JSONArray artists = o.getJSONArray("artists");
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < artists.length(); ++j) {
                            sb.append(artists.get(j).toString());
                            sb.append(" ");
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        String artistName = sb.toString();
                        String coverSmall = o.getString("cover_small");
                        String coverMedium = o.getString("cover_medium");
                        // TODO: fix this mess
                        String uri = BASE_URL + "/media/songs/play?id=" + id;
                        songs.add(new Song(id, songTitle, artistName,
                                albumTitle, duration, coverSmall, coverMedium, uri));
                    }

                    return new HttpResponseWithData<>(true, message, songs);
                } else
                    return new HttpResponseWithData<>(true, message, new ArrayList<>());
            } else {
                return new HttpResponseWithData<>(false, message, null);
            }
        }
        catch (JSONException e) {
            return null;
        }
    }

    public static HttpResponseWithData<List<Song>> getGenreSongs(Map<String, String> params) {
        String url = "/media/songs/genre";
        return getSongs(url, params);
    }

    public static HttpResponseWithData<List<Song>> getSongsByTitle(Map<String, String> params) {
        String url = "/media/songs/search";
        return getSongs(url, params);
    }

    public static HttpResponseWithData<List<Song>> getSongsByArtist(Map<String, String> params) {
        String url = "/media/songs/artist";
        return getSongs(url, params);
    }

    public static HttpResponseWithData<List<Song>> getAlbumSongs(Map<String, String> params) {
        String url = "/media/songs/album";
        return getSongs(url, params);
    }

    public static HttpResponseWithData<List<Song>> getPlaylistSongs(Map<String, String> params) {
        String url = "/media/songs/playlist";
        return getSongs(url, params);
    }
}
