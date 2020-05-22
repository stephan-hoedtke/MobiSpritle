package com.stho.mobispritle;

class Settings {

    private boolean useGravity = true;
    private boolean useRotation = false;
    private boolean showDigital = true;
    private boolean showAccelerationVector = false;

    boolean useGravitySensor() { return useGravity; }
    boolean useRotationSensor() { return useRotation; }
    boolean showDigital() { return showDigital; }
    boolean showAccelerationVector() { return showAccelerationVector; }

    void setUseGravity(boolean value) {
        useGravity = value;
    }
    void setUseRotation(boolean value) {
        useRotation = value;
    }
    void setShowDigital(boolean value) { showDigital = value; }
    void setShowAccelerationVector(boolean value) { showAccelerationVector = value; }
}
