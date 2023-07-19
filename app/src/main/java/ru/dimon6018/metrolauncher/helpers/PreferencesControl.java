package ru.dimon6018.metrolauncher.helpers;

//Use this class to control start menu

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.dimon6018.metrolauncher.content.Apps;

public class PreferencesControl implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String FILE_NAME = "MetroSettings";
    private static final String APP_PACKAGE = "app_package";
    private final Context mContext;
    private final SharedPreferences mPrefs;
    private PrefsListener mListener;

    public PreferencesControl(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(FILE_NAME, 0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
    }
    public void setListener(PrefsListener listener) {
        mListener = listener;
        if (mListener != null) {
            mPrefs.registerOnSharedPreferenceChangeListener(this);
        } else {
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }
    }
    public interface PrefsListener {
        void onPrefsChanged();
    }
    public void addApptoStart(String packag) {
        mPrefs.edit()
                .putString(APP_PACKAGE, packag)
                .apply();
    }
    public void removeAppFromStart(String packag) {
        mPrefs.edit().remove(packag).apply();
    }
    public List<Apps> getApps() {
        ArrayList<Apps> appsList = new ArrayList<>();
        Map<String, ?> map = mPrefs.getAll();
        for (String key : map.keySet()) {
            if (key.startsWith(APP_PACKAGE)) {
                String packag = key.substring(APP_PACKAGE.length());
                Log.e("PREFS", packag);
                Apps apps = new Apps();
                appsList.add(apps);
            }
        }
        return appsList;
    }
    public void addDefaultTiles() {
        mPrefs.edit()
                .putString(APP_PACKAGE, "com.android.settings")
                .apply();
    }
}
