package me.zingmars.dankpressandroid;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

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
import java.util.List;

/**
 * Created by zingmars on 28.10.2015.
 */
public class App extends Application {
    private static Context context;
    private static SharedPreferences preferences;
    static ArrayList<String> Headlines = new ArrayList<>();
    static ArrayList<String> Posts = new ArrayList<>();
    static Boolean ChangedDomain = false;

    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Context getAppContext() {
        return App.context;
    }
    public static SharedPreferences Prefs() {
        return App.preferences;
    }
    public static void ChangeDomain(String value) {
        final SharedPreferences.Editor editor = App.Prefs().edit();
        editor.putString("domain", value);
        editor.commit();

        App.ChangedDomain = true;
    }
    public static Boolean DomainStateCheck() {
        if(!ChangedDomain) return false;
        else {App.ChangedDomain = false; return true;}
    }

    public static Drawer createMenu(final Activity activity, int selection) {
        Drawer drawer = createMenu(activity);
        drawer.setSelection(-1); //TODO: Select an item in the menu by ID.
        //drawer.openDrawer();
        return drawer;
    }
    public static Drawer createMenu(final Activity activity) {
        //Create sidebar drawer
        String username = "Not Logged In";
        String email = "";
        if (App.loginState()) {
            username = App.currentUsername();
            email = "Logged in";
        }


        //Create the user header
        AccountHeader header = new AccountHeaderBuilder().withActivity(activity)
                .withHeaderBackground(ContextCompat.getDrawable(activity, R.drawable.material_wallpaper))
                .addProfiles(new ProfileDrawerItem().withName(username).withEmail(email))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        Log.i("APP", "Username:" + profile.getName().toString());
                        Log.i("APP", "Email:" + profile.getEmail().toString());
                        return false;
                    }
                })
                .build();

        //Build the items on the menu
        PrimaryDrawerItem Home = new PrimaryDrawerItem().withName("Home").withIdentifier(1).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                Intent mainScreen = new Intent(activity, MainActivity.class);
                activity.startActivity(mainScreen);
                return true;
            }
        });
        PrimaryDrawerItem Comments = new PrimaryDrawerItem().withName("Comments").withIdentifier(2).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                Intent comments = new Intent(activity, CommentsActivity.class);
                activity.startActivity(comments);
                return true;
            }
        });
        SecondaryDrawerItem Settings = new SecondaryDrawerItem().withName("Settings").withIdentifier(4).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                Intent settingsScreen = new Intent(activity, SettingsActivity.class);
                activity.startActivity(settingsScreen);
                return true;
            }
        });
        SecondaryDrawerItem TestArea = new SecondaryDrawerItem().withName("Test area").withIdentifier(5).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                Intent testArea = new Intent(activity, TestActivity.class);
                activity.startActivity(testArea);
                return true;
            }
        });
        SecondaryDrawerItem Exit = new SecondaryDrawerItem().withName("Exit").withIdentifier(6).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                activity.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
                return true;
            }
        });

        //Build the menu
        DrawerBuilder drawer = new DrawerBuilder().withActivity(activity)
                .withTranslucentStatusBar(false)
                .withAccountHeader(header);

        ArrayList<IDrawerItem> drawerItems = new ArrayList<>();
        drawerItems.add(Home);
        if(loginState()) {
            drawerItems.add(Comments);
        }

        drawerItems.add(new DividerDrawerItem());
        drawerItems.add(Settings);
        if(loginState() && isAdmin()) drawerItems.add(TestArea);
        drawerItems.add(Exit);

        drawer.withDrawerItems(drawerItems);
        return drawer.build();
    }
    //Account management functions
    public static boolean loginState() {
        return !preferences.getString("username", "").equals("");
    }
    public static boolean isAdmin() {
        return preferences.getString("privlvl", "").equals("admin");
    }
    public static String currentUsername() {
        return preferences.getString("username", "");
    }
    public static String currentUserID() { return preferences.getString("userid", ""); }
    //HTML Functions
    public static String HTMLGET(String URL) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            InputStream stream = new BufferedInputStream(connection.getInputStream());
            return convertStreamToString(stream);
        } catch (Exception e) {
            return null;
        }
    }
    public static String HTMLPOST(String URL, List<AbstractMap.SimpleEntry<String, String>> params) {
        try {
            URL url = new URL(URL);
            // TODO: https support
            // https://stackoverflow.com/a/29766510
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            String query = getQuery(params);
            connection.setFixedLengthStreamingMode(query.getBytes().length);
            PrintWriter output = new PrintWriter((connection.getOutputStream()));
            output.print(query);
            output.close();

            //int status = connection.getResponseCode();
            return convertStreamToString(connection.getInputStream());
        } catch (Exception e) {
            return null;
        }
    }
    private static String getQuery(List<AbstractMap.SimpleEntry<String, String>> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    //Helper functions
    static String convertStreamToString(java.io.InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            return "";
        }
    }

    //Post storage in memory
    public static void SavePostsInMemory(ArrayList<String> Headlines, ArrayList<String> Posts) {
        App.Headlines = Headlines;
        App.Posts = Posts;
    }
    public static ArrayList<String> GetPostHeadlines() {
        return App.Headlines;
    }
    public static ArrayList<String> GetPostIDs() {
        return App.Posts;
    }
}
