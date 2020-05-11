package com.stho.mobispritle;

public class Settings {

    private boolean useGravity = true;
    private boolean useRotation = false;

    boolean useGravitySensor() { return useGravity; }
    boolean useRotationSensor() { return useRotation; }

    void setUseGravity(boolean value) {
        useGravity = value;
    }
    void setUseRotation(boolean value) {
        useRotation = value;
    }
}
