package com.stho.mobispritle;

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

import com.stho.mobispritle.databinding.SettingsFragmentBinding;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private SettingsFragmentBinding binding;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = SettingsViewModel.build(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.settings_fragment, container, false);
        binding.buttonSave.setOnClickListener(view -> save());
        binding.switchUseGravity.setChecked(viewModel.getSettings().useGravitySensor());
        binding.switchUseRotation.setChecked(viewModel.getSettings().useRotationSensor());
        return binding.getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    private NavController findNavController() {
        return Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
    }

    private void save() {
        viewModel.getSettings().setUseGravity(binding.switchUseGravity.isChecked());
        viewModel.getSettings().setUseRotation(binding.switchUseRotation.isChecked());
        viewModel.save();
        findNavController().navigateUp();
    }
}
