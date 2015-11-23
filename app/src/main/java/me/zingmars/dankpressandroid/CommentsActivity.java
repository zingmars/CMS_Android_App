/**
 * Created by zingmars on 01.11.2015.
 */
package me.zingmars.dankpressandroid;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    final List<List<String>> Posts = new ArrayList<>();
    final List<String> ShortPostText = new ArrayList<>();
    String Domain = App.Prefs().getString("domain", "");

    class CommentDownloader implements Runnable {
        CommentDownloader() {
        }

        public void run() {
            try {
                //Download all of user's posts
                if(!Domain.equals("")) {
                    List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
                    String userID = App.Prefs().getString("userid", "");
                    AbstractMap.SimpleEntry<String, String> entryUsername = new AbstractMap.SimpleEntry<>("username", App.Prefs().getString("username", ""));
                    AbstractMap.SimpleEntry<String, String> entryToken = new AbstractMap.SimpleEntry<>("token", App.Prefs().getString("token", ""));
                    params.add(entryUsername);
                    params.add(entryToken);

                    Posts.clear();
                    ShortPostText.clear();

                    if(!userID.equals("")) {
                        String userCommentsJson = App.HTMLPOST(Domain+"/api/getUserComments/"+userID, params);

                        if(userCommentsJson != null && !userCommentsJson.equals("[]") && !userCommentsJson.equals("400 Bad Request")) {
                            JSONArray comments = new JSONArray(userCommentsJson);

                            for (int iii = 0; iii < comments.length(); iii++) {
                                JSONObject Object = comments.getJSONObject(iii);
                                String PostContents = Object.getString("comment");
                                String ID = Object.getString("id");

                                List<String> Post = new ArrayList<>();
                                Post.add(ID);
                                Post.add(PostContents);

                                String shortPostText = PostContents;
                                if(shortPostText.length() > 100) shortPostText.substring(0, 100);
                                ShortPostText.add(shortPostText);

                                Posts.add(Post);
                                Log.i("APP", "");
                            }
                            setPosts();
                        } else {
                            //No comments made
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView text = (TextView) findViewById(R.id.textView6);
                                    text.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        Log.i("APP", "Response: " + userCommentsJson);
                    } else {
                        throw new Exception("Erroneous username");
                    }
                } else {
                    throw new Exception("Erroneous domain");
                }

            } catch (Exception e) {
                Log.i("APP", e.toString());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_list);

        App.createMenu(this, 2);
        CommentDownloader Downloader = new CommentDownloader();
        Thread commentDownload = new Thread(Downloader);
        commentDownload.start();
    }

    private void setPosts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ListView List = (ListView) findViewById(R.id.listView2);

                if(List.getAdapter() != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((BaseAdapter) List.getAdapter()).notifyDataSetChanged();
                            List.requestLayout();
                        }
                    });

                }
                List.setVisibility(View.VISIBLE);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, ShortPostText);
                if(List.getAdapter() == null) List.setAdapter(adapter);

                List.setClickable(true);
                List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Open the comment view - post & comment
                        List.setVisibility(View.INVISIBLE);

                        final List<String> Comment = Posts.get(i);

                        final EditText commentEdit = (EditText) findViewById(R.id.editText);
                        commentEdit.setVisibility(View.VISIBLE);
                        commentEdit.setText(Comment.get(1));

                        final Button saveButton = (Button) findViewById(R.id.button3);
                        saveButton.setVisibility(View.VISIBLE);

                        final Button deleteButton = (Button) findViewById(R.id.button4);
                        deleteButton.setVisibility(View.VISIBLE);

                        saveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Save comment
                                final List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
                                final String commentID = Comment.get(0);

                                AbstractMap.SimpleEntry<String, String> entryUsername = new AbstractMap.SimpleEntry<>("username", App.Prefs().getString("username", ""));
                                AbstractMap.SimpleEntry<String, String> entryToken = new AbstractMap.SimpleEntry<>("token", App.Prefs().getString("token", ""));
                                AbstractMap.SimpleEntry<String, String> newComment = new AbstractMap.SimpleEntry<>("comment", commentEdit.getText().toString());
                                params.add(entryUsername);
                                params.add(entryToken);
                                params.add(newComment);


                                Runnable DL = new Runnable() {
                                    @Override
                                    public void run() {
                                        String postEdit = App.HTMLPOST(Domain + "/api/editComment/" + commentID, params);

                                        // Hide Edit
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                commentEdit.setVisibility(View.INVISIBLE);
                                                saveButton.setVisibility(View.INVISIBLE);
                                                deleteButton.setVisibility(View.INVISIBLE);
                                                List.setVisibility(View.VISIBLE);
                                                List.requestLayout();

                                                // Refresh
                                                CommentDownloader Downloader = new CommentDownloader();
                                                Thread commentDownload = new Thread(Downloader);
                                                commentDownload.start();
                                            }
                                        });

                                    }
                                };
                                Thread DLThread = new Thread(DL);
                                DLThread.start();
                            }
                        });
                        //Enable Swipe to refresh
                        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout2);
                        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                CommentDownloader Downloader = new CommentDownloader();
                                Thread commentDownload = new Thread(Downloader);
                                commentDownload.start();
                            }
                        });
                        refreshLayout.setRefreshing(false);

                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delete comment
                                final List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
                                final String commentID = Comment.get(0);

                                AbstractMap.SimpleEntry<String, String> entryUsername = new AbstractMap.SimpleEntry<>("username", App.Prefs().getString("username", ""));
                                AbstractMap.SimpleEntry<String, String> entryToken = new AbstractMap.SimpleEntry<>("token", App.Prefs().getString("token", ""));
                                params.add(entryUsername);
                                params.add(entryToken);

                                Runnable DL = new Runnable() {
                                    @Override
                                    public void run() {
                                        String removeComment = App.HTMLPOST(Domain + "/api/removeComment/" + commentID, params);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Hide Edit
                                                commentEdit.setVisibility(View.INVISIBLE);
                                                saveButton.setVisibility(View.INVISIBLE);
                                                deleteButton.setVisibility(View.INVISIBLE);
                                                List.setVisibility(View.VISIBLE);

                                                // Refresh
                                                CommentDownloader Downloader = new CommentDownloader();
                                                Thread commentDownload = new Thread(Downloader);
                                                commentDownload.start();
                                            }
                                        });
                                    }
                                };
                                Thread DLThread = new Thread(DL);
                                DLThread.start();
                            }
                        });
                    }
                });
            }
        });
    }
}