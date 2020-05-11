package com.stho.mobispritle;

import android.content.Context;

import java.util.Set;

public class Repository {

    private static Repository singleton = null;;
    private static Object lockObject = new Object();

    /*
    Get the single instance of the repository, if it exists already, otherwise create it and load the data using the context.
 */
    public static synchronized Repository getRepository(Context context) {
        if (singleton == null) {
            singleton = new Repository(context);
        }
        return singleton;
    }

    private final Settings settings = new Settings();

    private Repository(Context context) {
        loadSettings(context);
    }

    public void save(Context context) {
        saveSettings(context);
    }

    public Settings getSettings() {
        return settings;
    }

    private void loadSettings(Context context) {
        PreferencesManager preferencesManager = PreferencesManager.build(context);
        preferencesManager.load(settings);
    }

    private void saveSettings(Context context) {
        PreferencesManager preferencesManager = PreferencesManager.build(context);
        preferencesManager.save(settings);
    }
}
