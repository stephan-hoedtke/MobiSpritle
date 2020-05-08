package com.stho.mobispritle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;


public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<Float> azimuthLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> rollLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> pitchLiveData = new MutableLiveData<>();

    @SuppressWarnings("ConstantConditions")
    static HomeViewModel build(Fragment fragment) {
        return new ViewModelProvider(fragment.getActivity()).get(HomeViewModel.class);
    }

    public HomeViewModel(@NonNull Application application) {
        super(application);
        azimuthLiveData.setValue(0f);
        pitchLiveData.setValue(0f);
        rollLiveData.setValue(0.02f);
    }

    LiveData<Float> getAzimuthLD() { return azimuthLiveData; }
    LiveData<Float> getPitchLD() { return pitchLiveData; }
    LiveData<Float> getRollLD() { return rollLiveData; }
    LiveData<Double> getTranslationFactorLD() { return Transformations.map(rollLiveData, this::getTranslationFactor); }

    private static final float LOW_PASS_FILTER = 0.1f;

    @SuppressWarnings("ConstantConditions")
    void setAzimuth(float value) {
        float currentValue = azimuthLiveData.getValue();
        azimuthLiveData.postValue((1 - LOW_PASS_FILTER) * currentValue + LOW_PASS_FILTER * value);
    }

    @SuppressWarnings("ConstantConditions")
    void setPitch(float value) {
        float currentValue = pitchLiveData.getValue();
        pitchLiveData.postValue((1 - LOW_PASS_FILTER) * currentValue + LOW_PASS_FILTER * value);
    }

    @SuppressWarnings("ConstantConditions")
    void setRoll(float value) {
        float currentValue = rollLiveData.getValue();
        rollLiveData.postValue((1 - LOW_PASS_FILTER) * currentValue + LOW_PASS_FILTER * value);
    }

    static String toString(float value) {
        String sign = (value < 0) ? "-" : "+";
        return sign + String.format(Locale.ENGLISH, "%.2f", Math.abs(value));
    }

    private double getTranslationFactor(float roll) {
        double sign = Math.signum(roll);
        double factor = Math.min(0.8, Math.abs(roll));
        return sign * factor;
    }
}
