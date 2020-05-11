package com.stho.mobispritle;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.stho.mobispritle.databinding.HomeFragmentBinding;

/*
read: https://developer.android.com/guide/topics/sensors/sensors_position
 */
public class HomeFragment extends Fragment implements SensorEventListener {

    private HomeViewModel viewModel;
    private HomeFragmentBinding binding;
    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = HomeViewModel.build(this);
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false);
        viewModel.getAzimuthLD().observe(getViewLifecycleOwner(), this::updateAzimuthUI);
        viewModel.getPitchLD().observe(getViewLifecycleOwner(), this::updatePitchUI);
        viewModel.getRollLD().observe(getViewLifecycleOwner(), this::updateRollUI);
        viewModel.getTranslationFactorLD().observe(getViewLifecycleOwner(), this::translate);
        return binding.getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.getSettings().useGravitySensor() || viewModel.getSettings().useRotationSensor()) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
        if (viewModel.getSettings().useRotationSensor()) {
            Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (magneticField != null) {
                sensorManager.registerListener(this, magneticField,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,0, magnetometerReading.length);
        }
        updateOrientationAngles();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // ignore
    }

    @SuppressWarnings("ConstantConditions")
    private NavController findNavController() {
        return Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private void updateOrientationAngles() {
        if (viewModel.getSettings().useGravitySensor()) {
            getOrientation(accelerometerReading, orientationAngles);
            viewModel.update(orientationAngles, isPortrait());
        }
        if (viewModel.getSettings().useRotationSensor()) {
            getOrientation(accelerometerReading, magnetometerReading, orientationAngles);
            viewModel.update(orientationAngles, isPortrait());
        }
    }

    private boolean isPortrait() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    private void updateAzimuthUI(float pitch) {
        binding.textViewAzimuth.setText(Formatter.toAngleString(pitch));
    }

    private void updatePitchUI(float pitch) {
        binding.textViewPitch.setText(Formatter.toAngleString(pitch));
    }

    private void updateRollUI(float roll) {
        binding.textViewRoll.setText(Formatter.toAngleString(roll));
    }

    private void translate(double translationFactor) {
        int dx = (int) (-translationFactor * binding.levelBubble.getWidth());
        binding.levelBubble.setTranslationX(dx);
    }

    private void getOrientation(float[] accelerometerReading, float[] orientationAngles) {
        orientationAngles[0] = 0f;
        orientationAngles[1] = -(float)Math.atan2(accelerometerReading[1], accelerometerReading[2]);
        orientationAngles[2] = -(float)Math.atan2(accelerometerReading[0], accelerometerReading[2]);
    }

    private void getOrientation(float[] accelerometerReading, float[] magnetometerReading, float[] orientationAngles) {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }
}
