package lab.android.audiodementia.user;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public UserSession(Context context) {
        this.preferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        this.editor = preferences.edit();
//        editor.commit();
    }

    public void saveLogIn(String login, String pass, String token, String refresh, long id) {
        editor.putString("LOGIN", login);
        editor.commit();
        editor.putString("PASS", pass);
        editor.commit();
        editor.putString("TOKEN", token);
        editor.commit();
        editor.putString("REFRESH", refresh);
        editor.commit();
        editor.putLong("USER_ID", id);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return (getLogin() != null) || (getToken() != null);
    }

    public String getLogin()
    {
        if (!preferences.contains("LOGIN")) {
            return null;
        }
        else {
            return preferences.getString("LOGIN", null);
        }
    }

    public String getPass()
    {
        if (!preferences.contains("PASS")) {
            return null;
        }
        else {
            return preferences.getString("PASS", null);
        }
    }

    public String getToken()
    {
        if (!preferences.contains("TOKEN")) {
            return null;
        }
        else {
            return preferences.getString("TOKEN", null);
        }
    }

    public String getRefresh()
    {
        if (!preferences.contains("REFRESH")) {
            return null;
        }
        else {
            return preferences.getString("REFRESH", null);
        }
    }

    public long getId()
    {
        if (!preferences.contains("USER_ID")) {
            return -1;
        }
        else {
            return preferences.getLong("USER_ID", -1);
        }
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

}
