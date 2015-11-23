package me.zingmars.dankpressandroid;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CommentListItem extends BaseExpandableListAdapter {

    private Activity context;
    private Map<String, List<String>> commentCollection;
    private List<String> comments;
    private List<List<String>> commentInfo;
    private List<Integer> editableState;
    private final List<String> postIDs;

    public CommentListItem(Activity context, List<String> comments, Map<String, List<String>> commentCollection, List<List<String>> commentInfo, List<Integer> editableState, List<String> postIDs) {
        this.context = context;
        this.commentCollection = commentCollection;
        this.comments = comments;
        this.commentInfo = commentInfo;
        this.editableState = editableState;
        this.postIDs = postIDs;
    }

    public Object getChild(int groupPosition, int childPosition) {
        return commentCollection.get(comments.get(groupPosition)).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String comment = (String) getChild(groupPosition, childPosition);
        final LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.comments_item, null);
        }

        final TextView item = (TextView) convertView.findViewById(R.id.comment_content);
        final TextView content = (TextView) convertView.findViewById(R.id.comment_data);
        final EditText editComment = (EditText) convertView.findViewById(R.id.editComment);
        final Button saveButton = (Button) convertView.findViewById(R.id.button9);

        Button del = (Button) convertView.findViewById(R.id.button7);
        //TODO: Fix the edit box not appearing
        //TODO: Fix a bug where opening another comment while the edit mode is on will cause that comment to have the edit box instead.
        Button ed = (Button) convertView.findViewById(R.id.button8);
        if(editableState.get(groupPosition).equals(1)) {
            del.setVisibility(View.VISIBLE);
            ed.setVisibility(View.VISIBLE);

            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Delete comment
                    final List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
                    final String commentID = postIDs.get(groupPosition);
                    final String Domain = App.Prefs().getString("domain", "");

                    AbstractMap.SimpleEntry<String, String> entryUsername = new AbstractMap.SimpleEntry<>("username", App.Prefs().getString("username", ""));
                    AbstractMap.SimpleEntry<String, String> entryToken = new AbstractMap.SimpleEntry<>("token", App.Prefs().getString("token", ""));
                    params.add(entryUsername);
                    params.add(entryToken);

                    Runnable DL = new Runnable() {
                        @Override
                        public void run() {
                            String removeComment = App.HTMLPOST(Domain + "/api/removeComment/" + commentID, params);
                            // Remove it from the list
                            if(removeComment != null && removeComment.equals("Success")) {
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onGroupCollapsed(groupPosition);
                                        removeChild(comment, groupPosition);
                                        notifyDataSetChanged();
                                    }
                                });
                            } else {
                                Looper.prepare();
                                Toast toast = Toast.makeText(App.getAppContext(), "Failed to delete the comment!", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    };
                    Thread DLThread = new Thread(DL);
                    DLThread.start();
                }
            });
            ed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.getVisibility() == View.VISIBLE) {
                        //Create the edit field.
                        item.setVisibility(View.GONE);
                        editComment.setVisibility(View.VISIBLE);
                        saveButton.setVisibility(View.VISIBLE);

                        saveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Edit comment
                                final List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
                                final String commentID = postIDs.get(groupPosition);
                                final String Domain = App.Prefs().getString("domain", "");

                                AbstractMap.SimpleEntry<String, String> entryUsername = new AbstractMap.SimpleEntry<>("username", App.Prefs().getString("username", ""));
                                AbstractMap.SimpleEntry<String, String> entryToken = new AbstractMap.SimpleEntry<>("token", App.Prefs().getString("token", ""));
                                AbstractMap.SimpleEntry<String, String> newComment = new AbstractMap.SimpleEntry<>("comment", editComment.getText().toString());
                                params.add(entryUsername);
                                params.add(entryToken);
                                params.add(newComment);

                                Runnable DL = new Runnable() {
                                    @Override
                                    public void run() {
                                        String editCommentResponse = App.HTMLPOST(Domain + "/api/editComment/" + commentID, params);
                                        if(editCommentResponse != null && editCommentResponse.equals("Success")) {
                                            item.setText(editComment.getText().toString());
                                        } else {
                                            Looper.prepare();
                                            Toast toast = Toast.makeText(App.getAppContext(), "Failed to edit the comment!", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                        item.setText(editComment.getText().toString());
                                        item.setVisibility(View.VISIBLE);
                                        editComment.setText("");
                                        editComment.setVisibility(View.GONE);
                                        saveButton.setVisibility(View.GONE);
                                    }
                                };
                                Thread DLThread = new Thread(DL);
                                DLThread.start();
                            }
                        });
                    } else {
                        item.setVisibility(View.VISIBLE);
                        editComment.setText("");
                        editComment.setVisibility(View.GONE);
                        saveButton.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            del.setVisibility(View.GONE);
            ed.setVisibility(View.GONE);
        }

        item.setText(comment);
        content.setText("Comment by " + commentInfo.get(groupPosition).get(0) + " made on " + commentInfo.get(groupPosition).get(1) + ".");
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        return commentCollection.get(comments.get(groupPosition)).size();
    }

    public Object getGroup(int groupPosition) {
        return comments.get(groupPosition);
    }

    public int getGroupCount() {
        return comments.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String commentName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.comments_comment,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.comment);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(commentName);
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public boolean removeChild(String message, int groupPosition) {
        commentCollection.remove(message);
        comments.remove(groupPosition);
        commentInfo.remove(groupPosition);
        editableState.remove(groupPosition);
        postIDs.remove(groupPosition);
        return true;
    }
}
