package com.stho.mobispritle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SettingsViewModel extends AndroidViewModel {

    private Repository repository;

    @SuppressWarnings("ConstantConditions")
    static SettingsViewModel build(Fragment fragment) {
        SettingsViewModel viewModel = new ViewModelProvider(fragment.getActivity()).get(SettingsViewModel.class);
        viewModel.repository = Repository.getRepository(fragment.getContext());
        return viewModel;
    }

    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    void save() {
        repository.save(getApplication());
    }

    Settings getSettings() {
        return repository.getSettings();
    }
}
