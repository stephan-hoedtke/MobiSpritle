package com.stho.mobispritle.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stho.mobispritle.*
import com.stho.mobispritle.databinding.FragmentHomeBinding
import com.stho.mobispritle.library.OrientationSensorListener

class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var sensorListener: OrientationSensorListener
    private lateinit var resetButtonAnimation: ViewAnimation
    private lateinit var parallelSwitchAnimation: ViewAnimation
    private lateinit var orthogonalSwitchAnimation: ViewAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        sensorListener = OrientationSensorListener(requireContext(), viewModel.orientationFilter)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.reset.setOnClickListener { viewModel.reset() }
        binding.bubble.setOnDoubleTapListener(object : OnDoubleTapListener {
            override fun onDoubleTap() {
                viewModel.reset()
            }
        })
        binding.bubble.setOnRotateListener(object : OnRotateListener {
            override fun onRotate(delta: Double) {
                viewModel.rotate(delta)
            }

        })
        binding.info.setOnClickListener { viewModel.fix()  }
        binding.plus.setOnTouchListener(RepeatingTouchListener({ viewModel.rotate(+1.0) }))
        binding.minus.setOnTouchListener(RepeatingTouchListener({ viewModel.rotate(-1.0) }))
        binding.parallel.setOnClickListener { viewModel.setMode(Mode.BelowParallel) }
        binding.orthogonal.setOnClickListener { viewModel.setMode(Mode.BelowOrthogonal) }
        resetButtonAnimation = ViewAnimation.build(binding.reset)
        parallelSwitchAnimation = ViewAnimation.build(binding.parallel)
        orthogonalSwitchAnimation = ViewAnimation.build(binding.orthogonal)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.infoLD.observe(viewLifecycleOwner, ::onObserveInfo)
        viewModel.infoVisibilityLD.observe(viewLifecycleOwner, ::onObserveInfoVisibility)
        viewModel.switchVisibilityLD.observe(viewLifecycleOwner, ::onObserveSwitchVisibility)
        viewModel.ringAngleLD.observe(viewLifecycleOwner, ::onObserveRingAngle)
        viewModel.stateLD.observe(viewLifecycleOwner, ::onObserveState)
        viewModel.isParallelEnabled.observe(viewLifecycleOwner) { isEnabled -> onObserveIsEnabled(binding.parallel, isEnabled)}
        viewModel.isOrthogonalEnabled.observe(viewLifecycleOwner) { isEnabled -> onObserveIsEnabled(binding.orthogonal, isEnabled)}
    }

    override fun onPause() {
        super.onPause()
        sensorListener.onPause()
        resetButtonAnimation.cleanup()
        parallelSwitchAnimation.cleanup()
        orthogonalSwitchAnimation.cleanup()
    }

    override fun onResume() {
        super.onResume()
        sensorListener.onResume()
    }

    private fun onObserveInfo(info: String) {
        binding.info.text = info
    }

    private fun onObserveInfoVisibility(isVisible: Boolean) {
        if (isVisible) {
            resetButtonAnimation.show()
        } else {
            resetButtonAnimation.dismiss()
        }
    }

    private fun onObserveSwitchVisibility(isVisible: Boolean) {
        if (isVisible) {
            parallelSwitchAnimation.show()
            orthogonalSwitchAnimation.show()
        } else {
            parallelSwitchAnimation.dismiss()
            orthogonalSwitchAnimation.dismiss()
        }
    }

    private fun onObserveIsEnabled(view: View, isEnabled: Boolean) {
        view.isEnabled = isEnabled
        view.isActivated = isEnabled
    }

    private fun onObserveRingAngle(angle: Double) {
        binding.bubble.setRingAngle(angle)
    }

    private fun onObserveState(state: MainViewModel.State) {
        val calculator = BubbleCalculator(state.quaternion)
        if (canStillUsePreviousMode(previousMode = state.mode, newMode = calculator.getForceMode())) {
            val gamma = calculator.getGamma(state.mode, state.alpha)
            onUpdate(state.mode, state.alpha, gamma)
        }
    }

    private fun canStillUsePreviousMode(previousMode: Mode, newMode: Mode?): Boolean =
        newMode?.let {
            if (it == previousMode) {
                true
            } else if (it == Mode.BelowParallel && previousMode == Mode.BelowOrthogonal) {
                true
            } else {
                viewModel.setMode(it)
                false
            }
        } ?: true

    private fun onUpdate(mode: Mode, alpha: Double, gamma: Double) {
        when (mode) {
            Mode.Portrait -> {
                binding.bubble.apply {
                    setIsTop(true)
                    setGlassAngle(alpha)
                    setBubbleAngle(gamma)
                }
            }
            Mode.LandscapePositive -> {
                binding.bubble.apply {
                    setIsTop(true)
                    setGlassAngle(alpha - 90.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.LandscapeNegative -> {
                binding.bubble.apply {
                    setIsTop(true)
                    setGlassAngle(alpha + 90.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.BelowParallel -> {
                binding.bubble.apply {
                    setIsTop(false)
                    setGlassAngle(90.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.BelowOrthogonal -> {
                binding.bubble.apply {
                    setIsTop(false)
                    setGlassAngle(0.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.Above -> {
                binding.bubble.apply {
                    setIsTop(false)
                    setGlassAngle(90.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.TopDown -> {
                binding.bubble.apply {
                    setIsTop(true)
                    setGlassAngle(alpha + 180.0)
                    setBubbleAngle(gamma)
                }
            }
        }
        onUpdateTextOrientation(mode)
    }

    private fun onUpdateTextOrientation(mode: Mode) {
        val rotation = when (mode) {
            Mode.Portrait -> 0f
            Mode.LandscapeNegative -> 90f
            Mode.LandscapePositive -> 270f
            Mode.BelowParallel -> 0f
            Mode.BelowOrthogonal -> 0f
            Mode.Above -> 0f
            Mode.TopDown -> 180f
        }
        binding.info.rotation = rotation
        binding.reset.rotation = rotation
    }

}