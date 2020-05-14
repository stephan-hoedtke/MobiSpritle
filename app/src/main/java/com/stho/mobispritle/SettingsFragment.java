package com.stho.mobispritle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.stho.mobispritle.databinding.SettingsFragmentBinding;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private SettingsFragmentBinding binding;

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
        try {
            viewModel.getSettings().setUseGravity(binding.switchUseGravity.isChecked());
            viewModel.getSettings().setUseRotation(binding.switchUseRotation.isChecked());
            viewModel.save();
            findNavController().navigateUp();
        }
        catch (Exception ex) {
            showExceptionSnackBar(ex.getMessage());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void showExceptionSnackBar(final String exception) {
        View container = getActivity().findViewById(R.id.container);
        Snackbar snackbar = Snackbar.make(container, exception, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(getContext(), R.color.colorSecondaryDark));
        snackbar.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
        snackbar.show();
    }

}
