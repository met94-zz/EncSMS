package zls.mutek.encsms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by abara on 5/9/2017.
 * settings activity class
 */

public class SettingsActivity extends Activity {
    public static String KEY_MSG_HEADER;
    public static String MSG_HEADER_DEF;
    public static String KEY_DEFAULTAPP;
    public static String KEY_THEME;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(MainActivity.activeTheme == MainActivity.Themes.Light) {
            setTheme(R.style.AppThemeLight_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment(), getString(R.string.preferencesFragmentTag))
                .commit();


//        Button myToolbar = (Button)findViewById(R.id.button_refresh_theme);
/*
        myToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                getTheme().applyStyle(R.style.AppTheme, true);
            }
        });
*/
    }

/*
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs)
    {
        View view = super.onCreateView(parent, name, context, attrs);
        Button myToolbar = (Button)findViewById(R.id.button_refresh_theme);
        if(myToolbar != null)
        {
            myToolbar.setTextSize(10);
        }
        return view;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs)
    {
        View view = super.onCreateView(name, context, attrs);
        Button myToolbar = (Button)findViewById(R.id.button_refresh_theme);
        if(myToolbar != null)
        {
            myToolbar.setTextSize(10);
        }
        return view;
    }
*/
    public static void initializeResources(MainActivity activity)
    {
        KEY_MSG_HEADER = activity.getString(R.string.generalPreferences_msgheaderKey);
        MSG_HEADER_DEF = activity.getString(R.string.generalPreferences_msgheaderDef);
        KEY_DEFAULTAPP = activity.getString(R.string.generalPreferences_defaultappKey);
        KEY_THEME = activity.getString(R.string.generalPreferences_themeKey);

        activity.MsgHeader =  PreferenceManager.getDefaultSharedPreferences(activity).getString(SettingsActivity.KEY_MSG_HEADER, SettingsActivity.MSG_HEADER_DEF);
        activity.useDefaultApp =  PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(SettingsActivity.KEY_DEFAULTAPP, false);
    }
}
