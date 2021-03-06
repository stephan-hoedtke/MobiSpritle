package com.stho.mobispritle;

import android.content.Context;
import android.content.SharedPreferences;

class PreferencesManager {

    private final Context context;

    static PreferencesManager build(Context context) {
       return new PreferencesManager(context.getApplicationContext());
    }

    private PreferencesManager(Context context) {
        this.context = context;
    }

    private final static String KEY_GRAVITY = "GRAVITY";
    private final static String KEY_ROTATION = "ROTATION";
    private final static String KEY_SHOW_DIGITAL = "SHOW_DIGITAL";
    private final static String KEY_SHOW_ACCELERATION_VECTOR = "SHOW_ACCELERATION_VECTOR";

    void load(Settings settings) {
        SharedPreferences preferences = context.getSharedPreferences(null, Context.MODE_PRIVATE);
        settings.setUseGravity(preferences.getBoolean(KEY_GRAVITY, settings.useGravitySensor()));
        settings.setUseRotation(preferences.getBoolean(KEY_ROTATION, settings.useRotationSensor()));
        settings.setShowDigital(preferences.getBoolean(KEY_SHOW_DIGITAL, settings.showDigital()));
        settings.setShowAccelerationVector(preferences.getBoolean(KEY_SHOW_ACCELERATION_VECTOR, settings.showAccelerationVector()));
    }

    void save(Settings settings) {
        SharedPreferences preferences = context.getSharedPreferences(null, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_GRAVITY, settings.useGravitySensor());
        editor.putBoolean(KEY_ROTATION, settings.useRotationSensor());
        editor.putBoolean(KEY_SHOW_DIGITAL, settings.showDigital());
        editor.putBoolean(KEY_SHOW_ACCELERATION_VECTOR, settings.showAccelerationVector());
        editor.apply();
    }
}
