package com.stho.mobispritle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;


public class HomeViewModel extends AndroidViewModel {

    private Repository repository;
    private Acceleration acceleration;
    private final LowPassFilter lowPassFilter = new LowPassFilter();
    private final MutableLiveData<Float> azimuthLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> rollLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> pitchLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> angleLiveData = new MutableLiveData<>();


    @SuppressWarnings("ConstantConditions")
    static HomeViewModel build(Fragment fragment) {
        HomeViewModel viewModel = new ViewModelProvider(fragment.getActivity()).get(HomeViewModel.class);
        viewModel.repository = Repository.getRepository(fragment.getContext());
        viewModel.acceleration = new Acceleration();
        viewModel.initialize();
        return viewModel;
    }

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    private void initialize() {
        azimuthLiveData.setValue(0f);
        pitchLiveData.setValue(0f);
        rollLiveData.setValue(0f);
        angleLiveData.setValue(0f);
    }

    LiveData<Float> getAzimuthLD() { return azimuthLiveData; }
    LiveData<Float> getPitchLD() { return pitchLiveData; }
    LiveData<Float> getRollLD() { return rollLiveData; }
    LiveData<Float> getAngleLD() { return angleLiveData; }
    LiveData<Double> getTranslationFactorLD() { return Transformations.map(angleLiveData, this::getTranslationFactor); }

    Settings getSettings() {
        return repository.getSettings();
    }

    void update(float[] acceleration, boolean isPortrait) {

        Vector gravity = lowPassFilter.setAcceleration(acceleration);

        azimuthLiveData.postValue(gravity.x);
        pitchLiveData.postValue(gravity.y);
        rollLiveData.postValue(gravity.z);

        if (isPortrait) {
            setAngle(gravity.z);
        }
        else {
            setAngle(-gravity.y);
        }
    }

    private void setAngle(float angle) {
        if (getSettings().useAcceleration()) {
            acceleration.update(angle);
        }
        else {
            angleLiveData.postValue(angle);
        }
    }

    void updateAngle() {
        if (getSettings().useAcceleration()) {
            double angle = acceleration.getPosition();
            angleLiveData.postValue((float)angle);
        }
    }

    private double getTranslationFactor(float roll) {
        double sign = Math.signum(roll);
        double factor = Math.min(0.5, Math.abs(roll));
        return sign * factor;
    }
}
