package com.stho.mobispritle;

import android.os.SystemClock;

class LowPassFilter {

    private final Vector gravity = new Vector();
    private long startTimeNanos;
    private float count;

    LowPassFilter() {
        startTimeNanos = SystemClock.elapsedRealtimeNanos();
        count = 0;
        setAcceleration(new float[]{0f, 0f, 9.78f});
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

    private static final float NANOSECONDS = 1000000000f;

    private float getAverageTimeDifferenceInSeconds() {
        if (count < 2) {
            count = 2;
            return 0;
        } else {
            long nanos = SystemClock.elapsedRealtimeNanos() - startTimeNanos;
            return nanos / count++ / NANOSECONDS;
        }
    }
}
