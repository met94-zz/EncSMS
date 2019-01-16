package zls.mutek.encsms;

import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by abara on 5/4/2017.
 * custom adapter class
 */

class MyCursorAdapter extends SimpleCursorAdapter {

    private final LayoutInflater mInflater;
    private final MainActivity mActivity;

    MyCursorAdapter(MainActivity activity, int layout, Cursor c, String[] from,
                               int[] to, int flags) {
        super(activity, layout, c, from, to, flags);
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActivity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        if(MainActivity.state == MainActivity.WorkingStates.ListSMS) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        int layout;

        if (!this.getCursor().moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        int type = getItemViewType(position);
        if (type == 0) {
            layout = R.layout.simple_list_item_3;
        }
        else {
            layout = R.layout.list_item_chat_message;
        }
        if (convertView == null) { //there is no recycled view
            convertView = mInflater.inflate(layout, parent, false);
            viewHolder = createViewHolder(convertView);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
            if(/*position == 0 && */viewHolder.lastState != MainActivity.state) {
                convertView = null;
                return this.getView(position, convertView, parent);
            }
        }

        if(setViewHolder(convertView, viewHolder)) {
            int typeSMS = getCursor().getInt(getCursor().getColumnIndexOrThrow(SMSConsts.TypeCol));
            setAlignment(viewHolder, typeSMS == SMSConsts.SMSTypes.TYPE_OUTBOX);
        }
        super.bindView(convertView, mActivity, getCursor()); //so method set with adapter.setViewBinder is called
        return convertView;
    }

    private void setAlignment(ViewHolder holder, boolean isMe) {
        if (isMe) {
            holder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.START;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.START;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.START;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        setViewHolder(v, holder);
        return holder;
    }

    private boolean setViewHolder(View v, ViewHolder holder)
    {
        if(MainActivity.state == MainActivity.WorkingStates.ListSMS) {
            return false;
        }
        holder.lastState = MainActivity.state;
        if(holder.txtMessage == null)
        {
            holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        }
        if(holder.content == null)
        {
            holder.content = (LinearLayout) v.findViewById(R.id.content);
        }
        if(holder.contentWithBG == null)
        {
            holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        }
        if(holder.txtInfo == null)
        {
            holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        }
        if(holder.txtMessage == null || holder.content == null || holder.contentWithBG == null || holder.txtInfo == null) {
            return false;
        }
        return true;
    }

    private static class ViewHolder {
        MainActivity.WorkingStates lastState;
        TextView txtMessage;
        TextView txtInfo;
        public LinearLayout content;
        LinearLayout contentWithBG;
    }
}
