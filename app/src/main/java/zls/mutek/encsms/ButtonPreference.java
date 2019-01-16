package zls.mutek.encsms;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

/**
 * Created by abara on 6/16/2017.
 */

public class ButtonPreference extends Preference {

    public ButtonPreference(Context context)
    {
        super(context);
    }

    public ButtonPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /*
    public ButtonPreference(android.content.Context context, android.util.AttributeSet attrs)
    {
        super(context, attrs);
    }
    */

    public ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    /*
    ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    */

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        final Button refreshButton = (Button) view.findViewById(R.id.button_refresh_theme);

        if (!refreshButton.hasOnClickListeners()) {
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = getContext().getPackageManager().getLaunchIntentForPackage(getContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(i);
                    //getTheme().applyStyle(R.style.AppTheme, true);
                }
            });
        }
    }
}
