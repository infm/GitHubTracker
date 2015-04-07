package com.infmme.githubtracker.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.kohsuke.github.GHThread;

import java.util.ArrayList;
import java.util.List;

/**
 * infm created it with love on 4/7/15. Enjoy ;)
 */
public class NotificationsAdapter extends ArrayAdapter<GHThread> {
    private static final int VIEW_TYPE_COUNT = 2;

    private static final int VIEW_TYPE_NIL = 0;
    private static final int VIEW_TYPE_FIRST = 1;

    private LayoutInflater mLayoutInflater;

    public NotificationsAdapter(Context context, List<GHThread> objects) {
        super(context, 0, objects);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public NotificationsAdapter(Context context) {
        super(context, 0, new ArrayList<GHThread>());
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        GHThread currThread = getItem(position);
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.basic_list_item, parent, false);
            holder = new ViewHolder();
            holder.stubTextView = ((TextView) convertView.findViewById(R.id.listItemTextView));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.stubTextView.setText(currThread.getTitle());
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    private static class ViewHolder {
        public TextView stubTextView;
    }
}
