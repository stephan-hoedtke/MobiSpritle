package com.stho.mobispritle

import androidx.lifecycle.*
import com.stho.mobispritle.library.algebra.Quaternion
import com.stho.mobispritle.library.filter.IOrientationFilter
import com.stho.mobispritle.library.filter.OrientationFilter
import com.stho.mobispritle.library.algebra.Degree
import com.stho.mobispritle.ui.home.BubbleCalculator
import kotlin.math.abs
import kotlin.math.roundToInt

class MainViewModel : ViewModel() {

    class State(val alpha: Double, val mode: Mode, val quaternion: Quaternion)

    val orientationFilter: IOrientationFilter = OrientationFilter()

    private val ringAngleLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = 0.0 }
    private val modeLiveData: MutableLiveData<Mode> = MutableLiveData<Mode>().apply { value = Mode.BelowParallel }

    val ringAngleLD: LiveData<Double>
        get() = ringAngleLiveData

    private val currentRingAngle: Double
        get() = ringAngleLiveData.value ?: 0.0

    private val currentMode: Mode
        get() = modeLiveData.value ?: Mode.BelowParallel

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

    val isOrthogonalEnabled: LiveData<Boolean>
        get() = Transformations.map(modeLiveData) { mode -> mode == Mode.BelowParallel }

    val isParallelEnabled: LiveData<Boolean>
        get() = Transformations.map(modeLiveData) { mode -> mode == Mode.BelowOrthogonal }

    val switchVisibilityLD: LiveData<Boolean>
        get() = Transformations.map(modeLiveData) { mode -> mode == Mode.BelowOrthogonal || mode == Mode.BelowParallel }

    fun reset() {
        ringAngleLiveData.postValue(0.0)
    }

    fun fix() {
        stateLiveData.value?.also {
            val calculator = BubbleCalculator(it.quaternion)
            val beta = calculator.getTilt(it.mode)
            ringAngleLiveData.postValue(beta)
        }
    }

    fun rotate(deltaInDegree: Double) {
        val degree: Double = Degree.normalize(currentRingAngle - deltaInDegree)
        ringAngleLiveData.value = degree
    }

    fun setMode(value: Mode) {
        if (modeLiveData.value != value) {
            modeLiveData.postValue(value)
        }
    }
}
