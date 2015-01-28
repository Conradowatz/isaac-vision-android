package de.conradowatz.isaacvision;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.widget.Toast;

import java.util.ArrayList;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_UPDATE_CHANNEL = "update-channel";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        final Preference checkForUpdate = findPreference("check-for-update");
        checkForUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                checkForUpdate.setSelectable(false);
                checkForUpdate.setSummary("Checking...");
                new VersionUpdater(getActivity()){

                    @Override
                    void onFinish(boolean newVersionAvailable) {
                        checkForUpdate.setSelectable(true);
                        checkForUpdate.setSummary("");
                        if (!newVersionAvailable) Toast.makeText(getActivity(), "No new version available", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    void onError(String message) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    }
                }.start();

                return false;
            }
        });

        final Preference redownloadItems = findPreference("redownload-items");
        redownloadItems.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                redownloadItems.setSelectable(false);
                redownloadItems.setSummary("Please wait...");
                new ItemGetter(getActivity(), ItemGetter.GETTERMODE_DOWNLOAD){

                    @Override
                    void onFinished(ArrayList<Item>[] items) {
                        redownloadItems.setSelectable(true);
                        //TODO redownloadItems.setSummary(getString(R.string.???));
                        Toast.makeText(getActivity(), "Database updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    void onError(String message) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    }
                }.start();

                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    updatePreference(preferenceGroup.getPreference(j));
                }
            } else {
                updatePreference(preference);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key));
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
