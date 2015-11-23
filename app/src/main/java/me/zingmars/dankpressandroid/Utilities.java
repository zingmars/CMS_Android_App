/**
 * Created by zingmars on 20.10.2015.
 */
package me.zingmars.dankpressandroid;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//TODO: Move everything to the App class since it gives you the ability to make easy global vars.
public class Utilities {
    Map<String,String> combineListsIntoOrderedMap (List<String> keys, List<String> values) {
        if (keys.size() != values.size())
            throw new IllegalArgumentException ("Cannot combine lists with dissimilar sizes");
        Map<String,String> map = new LinkedHashMap<String,String>();
        for (int i=0; i<keys.size(); i++) {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }

    //Account management functions
    private boolean login(String username, String token, String privlvl, String userid) {
        try {
            // NOTE: Why you shouldn't use sharedpreferences: https://stackoverflow.com/a/23698241
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", username);
            editor.putString("token", token);
            editor.putString("privlvl", privlvl);
            editor.putString("userid", userid);
            editor.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean logout() {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            AbstractMap.SimpleEntry<String, String> username = new AbstractMap.SimpleEntry<>("username", preferences.getString("username", ""));
            AbstractMap.SimpleEntry<String, String> token = new AbstractMap.SimpleEntry<>("token", preferences.getString("token", ""));
            params.add(username);
            params.add(token);
            String response = App.HTMLPOST(App.Prefs().getString("domain", "")+"/api/logout", params);
            //TODO: Redo, this whole thing is flawed.
            if(response.equals("Logout successful")) {
                final SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean login(String username, String password) {
        //Login
        List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
        AbstractMap.SimpleEntry<String, String> entryUsername = new AbstractMap.SimpleEntry<>("username", username);
        AbstractMap.SimpleEntry<String, String> entryPassword = new AbstractMap.SimpleEntry<>("password", password);
        params.add(entryUsername);
        params.add(entryPassword);
        String response = App.HTMLPOST(App.Prefs().getString("domain", "")+"/api/login", params);

        if(response == null) return false;
        else {
            //Split the string and save the username, the token, and the admin state
            //It doesn't matter if the user spoofs the admin login, server checks for rights every request anyway.
            try {
                String[] data = response.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if(data[0] != null & data[1] != null) {
                    return this.login(username, data[0], data[1], data[2]);
                } else return false;
            } catch (Exception e) {
                return false;
            }
        }
    }
}