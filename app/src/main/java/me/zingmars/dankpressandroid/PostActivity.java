/**
 * Created by zingmars on 17.10.2015.
 */
package me.zingmars.dankpressandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity implements Post_Comments.OnFragmentInteractionListener {
    private static final String TAG = "App";
    String id = "";
    WebView Post;

    class PostDownloader implements Runnable {
        String domain;
        String id;
        PostDownloader(String domain, String id) {
            this.domain = domain;
            this.id = id;
        }

        public void run() {
            try {
                //Get the post
                final String line = App.HTMLGET(domain+"/API/post/"+id);

                //Display it
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject message = new JSONObject(line);
                            Post.loadData(message.getString("longbody"), "text/html", null);

                            final Button newCommentButton = (Button) findViewById(R.id.button5);
                            FragmentManager manager = getSupportFragmentManager();
                            final Post_Comments comments = (Post_Comments)manager.findFragmentById(R.id.comments);
                            final Button cancelButton = (Button) findViewById(R.id.button6);
                            final EditText commentEdit = (EditText) findViewById(R.id.editText2);
                            final WebView TextView2 = (WebView) findViewById(R.id.textView2);

                            if(App.loginState()) {
                                newCommentButton.setVisibility(View.VISIBLE);
                            }
                            Log.i(TAG, "Post displayed successfully.");

                            //Add comment
                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    commentEdit.setText("");
                                    comments.toggleComments();
                                    cancelButton.setVisibility(View.INVISIBLE);
                                    commentEdit.setVisibility(View.INVISIBLE);
                                    TextView2.setVisibility(View.VISIBLE);
                                }
                            });
                            newCommentButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //TODO: Make a fragment that's shared between the comments view and this.
                                    if(comments.checkVisiblity() == View.VISIBLE) {
                                        cancelButton.setVisibility(View.VISIBLE);
                                        commentEdit.setVisibility(View.VISIBLE);
                                        TextView2.setVisibility(View.GONE);
                                    } else {
                                        //Create a new comment
                                        final String Comment = commentEdit.getText().toString();
                                        if(!Comment.equals("")) {
                                            //Hide the keyboard
                                            InputMethodManager imm =
                                                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(commentEdit.getWindowToken(), 0);

                                            Runnable newComment = new Runnable() {
                                                @Override
                                                public void run() {
                                                    List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
                                                    String postID = id;
                                                    AbstractMap.SimpleEntry<String, String> entryUsername = new AbstractMap.SimpleEntry<>("username", App.Prefs().getString("username", ""));
                                                    AbstractMap.SimpleEntry<String, String> entryToken = new AbstractMap.SimpleEntry<>("token", App.Prefs().getString("token", ""));
                                                    AbstractMap.SimpleEntry<String, String> comment = new AbstractMap.SimpleEntry<>("comment", Comment);
                                                    params.add(entryUsername);
                                                    params.add(entryToken);
                                                    params.add(comment);

                                                    String newUser = App.HTMLPOST(domain+"/API/addComment/"+postID, params);
                                                    if (newUser != null && !newUser.equals("400 Bad Request")) {
                                                        // Refresh
                                                        comments.refreshComments();
                                                    } else {
                                                        Toast Error = Toast.makeText(App.getAppContext(), "Could not save a comment!", Toast.LENGTH_SHORT);
                                                        Error.show();
                                                    }
                                                }
                                            };
                                            Thread newCommentThread = new Thread(newComment);
                                            newCommentThread.start();
                                        }
                                        cancelButton.setVisibility(View.INVISIBLE);
                                        commentEdit.setVisibility(View.INVISIBLE);
                                        TextView2.setVisibility(View.VISIBLE);
                                    }
                                    commentEdit.setText("");
                                    comments.toggleComments();
                                }
                            });
                        } catch (Exception e) {
                            Log.i(TAG, "Comment failed to post.");
                        }
                    }
                });
            } catch (Exception e) {
                Log.i(TAG, "Error getting the post object: " + e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WebView Post = (WebView) findViewById(R.id.textView2);
                        Post.loadData(getString(R.string.DownloadError), "text/html", null);
                    }
                });
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Started the post view");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        App.createMenu(this, -1);

        //Default "Still loading message"
        Post = (WebView) findViewById(R.id.textView2);
        Post.loadData(getString(R.string.Loading), "text/html", null);

        //Get passed variables
        Bundle bundle = getIntent().getExtras();
        String domain = bundle.getString("domain");
        String title = bundle.getString("title");
        id = bundle.getString("id");

        //Get the view's elements
        TextView Title = (TextView)findViewById(R.id.textView3);
        Button BackButton = (Button)findViewById(R.id.button);

        //Make the back button go... back.
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Button pressed.");
                finish();
            }
        });

        //Get and set the post data
        Title.setText(title);
        PostDownloader downloader = new PostDownloader(domain, id);
        Thread downloaderThread = new Thread(downloader);
        downloaderThread.start();

        Log.i(TAG, "Started getting the post data");
    }

    @Override
    public void onFragmentInteraction(String String) {
    }
}
