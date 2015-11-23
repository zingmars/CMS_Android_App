package me.zingmars.dankpressandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post_Comments extends Fragment {
    private OnFragmentInteractionListener onFragmentInteraction;
    private static final String TAG = "App";
    View Creator;
    String id;

    public static Post_Comments newInstance() {
        Post_Comments fragment = new Post_Comments();
        return fragment;
    }
    public Post_Comments() {
        // Required empty public constructor
    }

    class CommentDownloader implements Runnable {
        String url;
        String id;
        CommentDownloader(String url, String id) {
            this.url = url; this.id = id;
        }

        public void run() {
            try {
                //Get the post
                Log.v(TAG, "Downloading comments");
                final String commentJSON = App.HTMLGET(url);
                if(commentJSON == null || commentJSON.equals("[]")) {
                    TextView CommentsText = (TextView) getActivity().findViewById(R.id.textView5);
                    CommentsText.setText(R.string.NoComments);
                } else {
                    try {
                        //Create the comment list
                        //http://theopentutorials.com/tutorials/android/listview/android-expandable-list-view-example/
                        //TODO: Join these arrays into one. At the time it just felt easier to just make a new array for each new piece of data.
                        //TODO: Review when sober.
                        final List<String> groupList = new ArrayList<>();
                        final Map<String, List<String>> commentCollection = new HashMap<>();
                        final List<List<String>> commentInfo = new ArrayList<>();
                        final List<Integer> editableState = new ArrayList<>();
                        final List<String> postIDs = new ArrayList<>();

                        //Go over all comments and prepare them for display
                        JSONArray comments = new JSONArray(commentJSON);
                        for (int iii = 0; iii < comments.length(); iii++) {
                            List<String> childList = new ArrayList<>();
                            List<String> thisCommentInfo = new ArrayList<>();

                            JSONObject commentData = comments.getJSONObject(iii);
                            String comment = commentData.getString("comment");
                            childList.add(comment);

                            if(App.loginState() && commentData.getString("username").equals(App.currentUsername())) {
                                editableState.add(1);
                            } else {
                                editableState.add(0);
                            }
                            postIDs.add(commentData.getString("id"));
                            //Cut the title if needed
                            if(comment.length() > 40) {
                                String trimmedComment = comment.substring(0, 40);
                                groupList.add(trimmedComment + "...");
                                commentCollection.put(trimmedComment + "...", childList);
                            } else {
                                groupList.add(comment);
                                commentCollection.put(comment, childList);
                            }

                            thisCommentInfo.add(0, commentData.getString("username"));
                            thisCommentInfo.add(1, commentData.getString("date"));
                            commentInfo.add(thisCommentInfo);
                        }
                        // Display the comments
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ExpandableListView listview = (ExpandableListView) Creator.findViewById(R.id.expandableListView);
                                final CommentListItem listAdapter = new CommentListItem(getActivity(), groupList, commentCollection, commentInfo, editableState, postIDs);

                                listview.setAdapter(listAdapter);

                            }
                        });
                    } catch (Exception e) {
                        Log.v(TAG, "Error while getting comments: " + e.toString());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView CommentsText = (TextView) getActivity().findViewById(R.id.textView5);
                                CommentsText.setVisibility(View.VISIBLE);
                                CommentsText.setText(R.string.InternalError);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.v(TAG, "Error while getting comments: " + e.toString());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView CommentsText = (TextView) getActivity().findViewById(R.id.textView5);
                        CommentsText.setVisibility(View.VISIBLE);
                        CommentsText.setText(R.string.NoComments);
                    }
                });
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Set the interaction listener
        try {
            onFragmentInteraction = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() +  ": Failed to set interaction listener");
        }
        Log.v(TAG, "Creating comments view");
        this.Creator = inflater.inflate(R.layout.fragment_post__comments, container, false);

        //Get the post's ID from the parent activity and get the comments
        Bundle data = getActivity().getIntent().getExtras();
        id = data.getString("id");
        CommentDownloader commentDownloader = new CommentDownloader(App.Prefs().getString("domain", "")+"/API/getComments/"+id, id);
        Thread downloaderThread = new Thread(commentDownloader);
        downloaderThread.start();

        // Inflate the layout for this fragment
        return Creator;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        onFragmentInteraction = null;
    }
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String string);
    }
    public void toggleComments() {
        ExpandableListView listview = (ExpandableListView) Creator.findViewById(R.id.expandableListView);
        TextView commentHeader = (TextView) Creator.findViewById(R.id.textView5);
        if(listview.getVisibility() == View.GONE) {
            listview.setVisibility(View.VISIBLE);
            commentHeader.setVisibility(View.VISIBLE);
        }
        else {
            listview.setVisibility(View.GONE);
            commentHeader.setVisibility(View.GONE);
        }
    }
    public Integer checkVisiblity() {
        TextView commentHeader = (TextView) Creator.findViewById(R.id.textView5);
        return commentHeader.getVisibility();
    }
    public void refreshComments() {
        CommentDownloader commentDownloader = new CommentDownloader(App.Prefs().getString("domain", "")+"/API/getComments/"+id, id);
        Thread downloaderThread = new Thread(commentDownloader);
        downloaderThread.start();
    }
}
