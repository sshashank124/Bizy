package com.shashank.singh.bizy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;


public class HelpExpandableListAdapter extends BaseExpandableListAdapter {

    //region #variables
    private Context ctx;
    private List<String> helpGroupTitle;
    private HashMap<String, List<String>> helpChildText;
    private static LayoutInflater inflater = null;
    //endregion #variables

    //region #CONSTANTS

    //endregion #CONSTANTS

    public HelpExpandableListAdapter(Context context, List<String> hTitle,
                                     HashMap<String, List<String>> hText) {
        this.ctx = context;
        this.helpGroupTitle = hTitle;
        this.helpChildText = hText;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public String getChild(int groupPosition, int childPosititon) {
        return this.helpChildText.get(this.helpGroupTitle.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null)
            convertView = inflater.inflate(R.layout.help_child, parent, false);

        final String childText = (String) getChild(groupPosition, childPosition);
        TextView childTextView = (TextView) convertView.findViewById(R.id.help_child_text);
        childTextView.setText(childText);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.helpChildText.get(this.helpGroupTitle.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return this.helpGroupTitle.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.helpGroupTitle.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.help_parent, parent, false);
        }

        String parentText = getGroup(groupPosition);

        TextView parentTextView = (TextView) convertView.findViewById(R.id.help_parent_text);
        parentTextView.setText(parentText);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}