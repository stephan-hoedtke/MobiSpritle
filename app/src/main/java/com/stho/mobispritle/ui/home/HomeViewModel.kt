package com.stho.mobispritle.ui.home

import androidx.lifecycle.*
import com.stho.mobispritle.Mode
import com.stho.mobispritle.library.algebra.Orientation
import com.stho.mobispritle.library.algebra.Quaternion
import com.stho.mobispritle.library.algebra.RotationMatrix
import com.stho.mobispritle.library.algebra.Vector
import com.stho.mobispritle.library.filter.IOrientationFilter
import com.stho.mobispritle.library.filter.OrientationFilter
import com.stho.myorientation.library.algebra.Degree
import kotlin.math.abs
import kotlin.math.roundToInt

class HomeViewModel : ViewModel() {

    class State(val alpha: Double, val mode: Mode, val quaternion: Quaternion)

    val orientationFilter: IOrientationFilter = OrientationFilter()

    private val ringAngleLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = 0.0 }
    private val modeLiveData: MutableLiveData<Mode> = MutableLiveData<Mode>().apply { value = Mode.Below }

    val ringAngleLD: LiveData<Double>
        get() = ringAngleLiveData

    private val currentRingAngle: Double
        get() = ringAngleLiveData.value ?: 0.0

    private val currentMode: Mode
        get() = modeLiveData.value ?: Mode.Below

    private val currentOrientation: Quaternion
        get() = orientationFilter.quaternionLD.value ?: Quaternion.default

    private val stateLiveData: MediatorLiveData<State> = MediatorLiveData<State>().also { mediator ->
        mediator.addSource(ringAngleLD) { alpha ->
            mediator.postValue(State(alpha, currentMode, currentOrientation))
        }
        mediator.addSource(orientationFilter.quaternionLD) { quaternion ->
            mediator.postValue(State(currentRingAngle, currentMode, quaternion))
        }
        mediator.addSource(modeLiveData) { mode ->
            mediator.postValue(State(currentRingAngle, mode, currentOrientation))
        }
    }

    val stateLD: LiveData<State>
        get() = stateLiveData

    private val degreeLD: LiveData<Int>
        get() = Transformations.map(ringAngleLiveData) { angle -> abs(Degree.normalizeTo180(angle).roundToInt()) }

    val infoLD: LiveData<String>
        get() = Transformations.map(degreeLD) { degree -> "$degree°" }

    val infoVisibilityLD: LiveData<Boolean>
        get() = Transformations.map(degreeLD) { degree -> degree > 0 }

    fun reset() {
        ringAngleLiveData.postValue(0.0)
    }

    fun rotate(deltaInDegree: Double) {
        val degree: Double = Degree.normalize(assureValueOrAssumeZero(ringAngleLiveData.value) - deltaInDegree)
        ringAngleLiveData.value = degree
    }

    fun setMode(value: Mode) {
        if (modeLiveData.value != value) {
            modeLiveData.postValue(value)
        }
    }

    private fun assureValueOrAssumeZero(value: Double?): Double =
        value ?: 0.0

    private fun assureValueOrAssumeTrue(value: Boolean?): Boolean =
        value ?: false
}
