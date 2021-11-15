package com.stho.mobispritle.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stho.mobispritle.BubbleView
import com.stho.mobispritle.Mode
import com.stho.mobispritle.ViewAnimation
import com.stho.mobispritle.databinding.FragmentHomeBinding
import com.stho.mobispritle.library.OrientationSensorListener

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var sensorListener: OrientationSensorListener
    private lateinit var infoAnimation: ViewAnimation
    private lateinit var resetButtonAnimation: ViewAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        sensorListener = OrientationSensorListener(requireContext(), homeViewModel.orientationFilter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.reset.setOnClickListener { homeViewModel.reset() }
        binding.bubble.setOnDoubleTapListener(object : BubbleView.OnDoubleTapListener {
            override fun onDoubleTap() {
                homeViewModel.reset()
            }
        })
        binding.bubble.setOnRotateListener(object : BubbleView.OnRotateListener {
            override fun onRotate(delta: Double) {
                homeViewModel.rotate(-delta)
            }

        })
        infoAnimation = ViewAnimation.build(binding.info)
        resetButtonAnimation = ViewAnimation.build(binding.reset)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.infoLD.observe(viewLifecycleOwner) { info -> onObserveInfo(info) }
        homeViewModel.infoVisibilityLD.observe(viewLifecycleOwner) { isVisible -> onObserveInfoVisibility(isVisible) }
        homeViewModel.ringAngleLD.observe(viewLifecycleOwner) { angle -> onObserveRingAngle(angle) }
        homeViewModel.stateLD.observe(viewLifecycleOwner) { state -> onObserveState(state) }
    }

    override fun onPause() {
        super.onPause()
        sensorListener.onPause()
        infoAnimation.cleanup()
        resetButtonAnimation.cleanup()
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
            infoAnimation.show()
            resetButtonAnimation.show()
        }
        else {
            infoAnimation.dismiss()
            resetButtonAnimation.dismiss()
        }
    }

    private fun onObserveRingAngle(angle: Double) {
        binding.bubble.setRingAngle(angle)
    }

    private fun onObserveState(state: HomeViewModel.State) {
        val calculator = BubbleCalculator(state.quaternion)
        if (canStillUsePreviousMode(previousMode = state.mode, newMode = calculator.getForceMode())) {
            val gamma = calculator.getGamma(state.mode, state.alpha)
            onUpdate(state.mode, gamma)
        }
    }

    private fun canStillUsePreviousMode(previousMode: Mode, newMode: Mode?): Boolean =
        newMode?.let {
            if (it == previousMode) {
                true
            } else {
                homeViewModel.setMode(it)
                false
            }
        } ?: true

    private fun onUpdate(mode: Mode, gamma: Double) {
        when (mode) {
            Mode.Portrait -> {
                binding.bubble.apply {
                    setIsTop(true)
                    setIsMirror(false)
                    setGlassAdjustmentAngle(0.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.LandscapePositive -> {
                binding.bubble.apply {
                    setIsTop(true)
                    setIsMirror(false)
                    setGlassAdjustmentAngle(-90.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.LandscapeNegative -> {
                binding.bubble.apply {
                    setIsTop(true)
                    setIsMirror(false)
                    setGlassAdjustmentAngle(90.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.Below -> {
                binding.bubble.apply {
                    setIsTop(false)
                    setIsMirror(false)
                    setGlassAdjustmentAngle(-90.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.Above -> {
                binding.bubble.apply {
                    setIsTop(false)
                    setIsMirror(false)
                    setGlassAdjustmentAngle(90.0)
                    setBubbleAngle(gamma)
                }
            }
            Mode.TopDown -> {
                binding.bubble.apply {
                    setIsTop(true)
                    setIsMirror(false)
                    setGlassAdjustmentAngle(180.0)
                    setBubbleAngle(gamma)
                }
            }
        }
        onUpdateRotation(mode)
    }

    private fun onUpdateRotation(mode: Mode) {
        val rotation = when (mode) {
            Mode.Portrait -> 0f
            Mode.LandscapeNegative -> 90f
            Mode.LandscapePositive -> 270f
            Mode.Below -> 0f
            Mode.Above -> 0f
            Mode.TopDown -> 180f
        }
        binding.info.rotation = rotation
        binding.reset.rotation = rotation
    }

}