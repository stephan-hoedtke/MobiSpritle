package com.stho.mobispritle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


public class LowPassFilter {

    private final Vector gravity = new Vector();
    private float startTime = System.nanoTime();
    private float time = System.nanoTime();
    private float count = 0;

    LowPassFilter() {
        this.setAcceleration(new float[]{0.1f, 0.2f, 9.78f});
    }

    Vector setAcceleration(float[] acceleration) {
        float dt = getAverageTimeDifferenceInSeconds();
        lowPassFilter(acceleration, dt);
        return gravity;
    }

    private static final float TIME_CONSTANT = 0.18f;

    private void lowPassFilter(float[] acceleration, float dt) {
        if (dt > 0) {
            float alpha = dt / (TIME_CONSTANT + dt);
            gravity.x += alpha * (acceleration[0] - gravity.x);
            gravity.y += alpha * (acceleration[1] - gravity.y);
            gravity.z += alpha * (acceleration[2] - gravity.z);
        } else {
            gravity.setValues(acceleration);
        }
    }

    private static final float NANOSECONDS = 1000000000.0f;

    private float getAverageTimeDifferenceInSeconds() {
        System.nanoTime();
        if (count < 2) {
            count = 2;
            return 0;
        } else {
            float newTime = System.nanoTime();
            return (newTime - startTime) / NANOSECONDS / count++;
        }
    }
}
