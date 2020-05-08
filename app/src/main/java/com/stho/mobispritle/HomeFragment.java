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
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
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

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private void updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        if (isPortrait()) {
            viewModel.setAzimuth(orientationAngles[0]);
            viewModel.setPitch(orientationAngles[1]);
            viewModel.setRoll(orientationAngles[2]);
        }
        else {
            viewModel.setAzimuth(orientationAngles[0]);
            viewModel.setPitch(orientationAngles[2]);
            viewModel.setRoll(-orientationAngles[1]);
        }
    }

    private boolean isPortrait() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    private void updateAzimuthUI(float pitch) {
        binding.textViewAzimuth.setText(HomeViewModel.toString(pitch));
    }

    private void updatePitchUI(float pitch) {
        binding.textViewPitch.setText(HomeViewModel.toString(pitch));
    }

    private void updateRollUI(float roll) {
        binding.textViewRoll.setText(HomeViewModel.toString(roll));
    }

    private void translate(double translationFactor) {
        int dx = (int) (-translationFactor * binding.levelBubble.getWidth());
        binding.levelBubble.setTranslationX(dx);
    }
}
