package com.infmme.githubtracker.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.kohsuke.github.GHThread;

import java.io.IOException;
import java.util.ArrayList;

/**
 * infm created it with love on 4/7/15. Enjoy ;)
 */
public class NotificationsAdapter extends ArrayAdapter<GHThread> {
    private static final int VIEW_TYPE_COUNT = 2;

    private static final int VIEW_TYPE_NIL = 0;
    private static final int VIEW_TYPE_FIRST = 1;

    private LayoutInflater mLayoutInflater;

    public NotificationsAdapter(Context context) {
        super(context, 0, new ArrayList<GHThread>());
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        GHThread currThread = getItem(position);
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.basic_list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            holder.timeLapsedTextView.setText(currThread.getUpdatedAt().toString());
            holder.mainMessageTextView.setText(currThread.getTitle());
            holder.detailedMessageTextView.setText(currThread.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        public ImageView eventTypeImageView;

        public LinearLayout infoLayout;
        public TextView timeLapsedTextView;
        public TextView mainMessageTextView;

        public LinearLayout additionalInfoLayout;
        public ImageView userImageView;
        public TextView detailedMessageTextView;

        public ViewHolder(View view) {
            eventTypeImageView = (ImageView) view.findViewById(R.id.listItemEventType);
            infoLayout = (LinearLayout) view.findViewById(R.id.listItemInfo);
            timeLapsedTextView = (TextView) infoLayout.findViewById(R.id.infoTimeLapsed);
            mainMessageTextView = (TextView) infoLayout.findViewById(R.id.infoMainMessage);
            additionalInfoLayout = (LinearLayout) infoLayout
                    .findViewById(R.id.listItemAdditionalInfo);
            userImageView = (ImageView) additionalInfoLayout.findViewById(R.id.infoUserImage);
            detailedMessageTextView = (TextView) additionalInfoLayout
                    .findViewById(R.id.infoDetailedMessage);
        }
    }
}
