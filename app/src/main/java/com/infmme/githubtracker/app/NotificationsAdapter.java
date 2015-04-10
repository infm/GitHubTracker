package com.infmme.githubtracker.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.infmme.githubtracker.app.util.GHThreadPreview;
import com.squareup.picasso.Picasso;

/**
 * infm created it with love on 4/7/15. Enjoy ;)
 */
public class NotificationsAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_COUNT = 2;

    private static final int VIEW_TYPE_NIL = 0;
    private static final int VIEW_TYPE_FIRST = 1;

    private LayoutInflater mLayoutInflater;

    public NotificationsAdapter(Context context) {
        super(context, null, 0);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.basic_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.update(GHThreadPreview.fromCursor(cursor), mContext);
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

        public void update(GHThreadPreview curr, Context context) {
            timeLapsedTextView.setText(curr.timeLapsed);
            mainMessageTextView.setText(curr.mainMessage);
            detailedMessageTextView.setText(curr.detailedMessage);

            Picasso.with(context)
                   .load(curr.eventTypeResId)
                   .into(eventTypeImageView);

            if (!TextUtils.isEmpty(curr.userPicPath))
                Picasso.with(context)
                       .load(curr.userPicPath)
                       .resize(100, 100)
                       .into(userImageView);
        }
    }
}
