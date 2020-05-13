package com.stho.mobispritle;

class Settings {

    private boolean useGravity = true;
    private boolean useRotation = false;
    private boolean useAcceleration = true;

    boolean useGravitySensor() { return useGravity; }
    boolean useRotationSensor() { return useRotation; }
    boolean useAcceleration() { return useAcceleration; }

    void setUseGravity(boolean value) {
        useGravity = value;
    }
    void setUseRotation(boolean value) {
        useRotation = value;
    }
    void setUseAcceleration(boolean value) { useAcceleration = value; }
}
