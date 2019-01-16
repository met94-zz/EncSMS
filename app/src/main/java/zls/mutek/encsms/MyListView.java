package zls.mutek.encsms;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ListView;

/**
 * Created by abara on 5/17/2017.
 * custom ListView class
 */

public class MyListView extends ListView {
    private final Context mContext;

    boolean bShouldScrollTop = false;
    boolean bShouldResetDivider = false;
    boolean bShouldMarkTopItem = false;
    boolean bShouldChangeSendToAddButton = false;

    public MyListView(Context context)
    {
        super(context);
        mContext = context;
    }

    public  MyListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        //below will be called after listview is updated with adapter

        if (bShouldScrollTop) {
            setSelection(0);
            bShouldScrollTop = false;
        }
        if (bShouldResetDivider) {
            bShouldResetDivider = false;
            Utils.setDefaultDivider(this);
        }
        if(bShouldMarkTopItem) {
            bShouldMarkTopItem = false;
            View item = null;
            if(MainActivity.state == MainActivity.WorkingStates.ListSMS) {
                item = getChildAt(0); //top item
            } else if (MainActivity.state == MainActivity.WorkingStates.ListMessages) {
                item = getChildAt(getLastVisiblePosition()-getFirstVisiblePosition()); //bottom item
            }
            if(item != null) {
                AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
                anim.setDuration(1000);
                anim.setRepeatCount(5); //needs to be odd because of reverse repeat mode
                anim.setRepeatMode(Animation.REVERSE);
                item.startAnimation(anim);
            }
        }
        /*
        if(bShouldChangeSendToAddButton) {
            bShouldChangeSendToAddButton = false;
            if(mContext instanceof MainActivity) {
                MainActivity activity = (MainActivity)mContext;
                activity.changeSendButtonToAddButton(activity.send_addSMSButton); //slow transition
            }
        }
        */
    }
}
