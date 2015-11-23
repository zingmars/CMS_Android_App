//I really have no idea what I'm doing. Enjoy.
package me.zingmars.dankpressandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "App";
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
    SwipeRefreshLayout swipeRefreshLayout;
    Boolean error = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        //Default settings
        if (preferences.getString("domain", "").equals("")) {
            App.ChangeDomain("http://192.168.1.2/dankpress");
        }
        Log.i(TAG, "Initiated.");

        //Create a menu
        App.createMenu(this, 1);

        //Get posts
        Log.i(TAG, "Downloading post list.");
        RefreshPosts(false);

        //TODO: Refactor the code
    }

    private class RSSDownloader implements Runnable {
        MainActivity Parent;
        Boolean Forced;

        RSSDownloader(MainActivity Parent, Boolean Forced) {
            this.Parent = Parent;
            this.Forced = Forced;
        }

        @Override
        public void run() {
            //RSS downloader code from https://androidresearch.wordpress.com/2012/01/21/creating-a-simple-rss-application-in-android/
            try {
                if (App.GetPostHeadlines().size() == 0 || Forced || App.DomainStateCheck()) {
                    DownloadPosts();
                }
                BindPostsToList(Parent);
            } catch (Exception e) {
                Log.i(TAG, "RSS Feed error: " + e.toString());
            }
        }
    }

    private void DownloadPosts() {
        try {
            //Temporary variables.
            final ArrayList<String> Headlines = new ArrayList<>();
            final ArrayList<String> Posts = new ArrayList<>();

            //Get the RSS feed
            URL feed = new URL(preferences.getString("domain", "") + "/posts/index.rss");
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            parserFactory.setNamespaceAware(false);
            XmlPullParser parser = parserFactory.newPullParser();
            System.setProperty("java.net.useSystemProxies", "true");
            parser.setInput(feed.openConnection().getInputStream(), "UTF_8");
            boolean insideItem = false;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equalsIgnoreCase("item")) {
                        insideItem = true;
                    } else if (parser.getName().equalsIgnoreCase("title")) {
                        if (insideItem) Headlines.add(parser.nextText());
                    } else if (parser.getName().equalsIgnoreCase("link")) {
                        if (insideItem) {
                            // Extract the post's URL
                            String url = parser.nextText();
                            String postID = url.substring(url.indexOf("/view/") + 6, url.lastIndexOf("/"));
                            Posts.add(postID);
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                    insideItem = false;
                }
                eventType = parser.next();
            }
            Log.i(TAG, "Successfully downloaded the RSS feed");

            App.SavePostsInMemory(Headlines, Posts);
            error = false;
        } catch (Exception e) {
            DownloadError(e);
        }
    }

    private void BindPostsToList(final MainActivity Parent) {
        if(error) return;
        try {
            final ArrayList<String> Headlines = App.GetPostHeadlines();
            final ArrayList<String> Posts = App.GetPostIDs();
            final String domain = preferences.getString("domain", "");

            if (App.GetPostHeadlines().size() == 0) {
                //There are no posts on the remote server
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView view = (TextView) findViewById(R.id.textView);
                        view.setText(R.string.NoPosts);
                    }
                });
            } else {
                //Bind the data to the list
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ListView List = (ListView) findViewById(R.id.listView);
                        findViewById(R.id.textView).setVisibility(View.GONE);
                        List.setVisibility(View.VISIBLE);

                        //Set the ListView's data
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Parent, android.R.layout.simple_list_item_1, Headlines);
                        List.setAdapter(adapter);

                        //Make clicking on the list actually do things.
                        List.setClickable(true);
                        List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Log.i(TAG, "Pressed a thing. Name: " + Headlines.get(i) + ", ID:" + Posts.get(i));
                                Intent intent = new Intent(Parent, PostActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("domain", domain);
                                bundle.putString("title", Headlines.get(i));
                                bundle.putString("id", Posts.get(i));
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });

                        //Enable Swipe to refresh
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                RefreshPosts(true);
                            }
                        });
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        } catch (Exception e) {
            DownloadError(e);
        }
    }

    private void DownloadError(Exception e) {
        error = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Hide elements
                findViewById(R.id.textView).setVisibility(View.VISIBLE);
                findViewById(R.id.listView).setVisibility(View.GONE);

                //Put up an error message
                TextView text = (TextView) findViewById(R.id.textView);
                text.setText(R.string.DownloadError);

                //Reset the post database so that BindToPostList doesn't bind any previously existing copies.
                App.SavePostsInMemory(new ArrayList<String>(), new ArrayList<String>());
            }
        });

        Log.i(TAG, "Error while downloading the RSS feed: " + e.toString());
    }

    private void RefreshPosts(Boolean Forced) {
        if(Forced) Log.i(TAG, "Refreshing post list");

        RSSDownloader Downloader = new RSSDownloader(this, Forced);
        Thread postDownload = new Thread(Downloader);
        postDownload.start();
    }
}
