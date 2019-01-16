package zls.mutek.encsms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by abara on 5/9/2017.
 * settings fragment class
 */

public class SettingsFragment extends PreferenceFragment {

    SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mSettingsListener = getOnSharedPreferenceChange();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(mSettingsListener);

        /*
        Preference preference = findPreference(getString(R.string.generalPreferences_themeRefreshKey));
        if (preference != null) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().getTheme().applyStyle(R.style.AppTheme, true);
                    return true;
                }
            });
        }
        */
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mSettingsListener.onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(getActivity()), SettingsActivity.KEY_THEME); //set switch text
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View ret = super.onCreateView(inflater, container, savedInstanceState);

        if(ret != null) {
            //Button myToolbar = (Button) ret.findViewById(R.id.button_refresh_theme);

        }

        return ret;
    }

    /**********************************
     *
     * On Preference Changed
     *
     **********************************/

    private SharedPreferences.OnSharedPreferenceChangeListener getOnSharedPreferenceChange() {
        return new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals((SettingsActivity.KEY_THEME))) {
                    if(isAdded()) {
                        SwitchPreference switchPreference = (SwitchPreference) findPreference(getString(R.string.generalPreferences_themeKey));
                        if (switchPreference != null) {
                            if (sharedPreferences.getBoolean(key, true)) {
                                switchPreference.setTitle(R.string.generalPreferences_themeDark);
                            } else {
                                switchPreference.setTitle(R.string.generalPreferences_themeLight);
                            }
                        }
                    }
                }
            }
        };
    }
}
