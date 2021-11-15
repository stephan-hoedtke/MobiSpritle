package com.stho.mobispritle.library.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stho.mobispritle.library.algebra.Quaternion
import kotlin.math.abs


class OrientationFilter : IOrientationFilter {

    private val orientationQuaternionLiveData: MutableLiveData<Quaternion> = MutableLiveData<Quaternion>()

    override val quaternionLD: LiveData<Quaternion>
        get() = orientationQuaternionLiveData

    override fun onOrientationAnglesChanged(newOrientation: Quaternion) {
        if (orientationQuaternionLiveData.value == null || areDifferent(orientationQuaternionLiveData.value!!, newOrientation)) {
            orientationQuaternionLiveData.postValue(newOrientation)
        }
    }

    companion object {

        private fun areDifferent(a: Quaternion, b: Quaternion): Boolean =
            areDifferent(a.x, b.x) || areDifferent(a.y, b.y) || areDifferent(a.z, b.z) || areDifferent(a.s, b.s)

        private fun areDifferent(a: Double, b: Double): Boolean =
            abs(a - b) > EPSILON

        private const val EPSILON: Double = 0.000000001
    }
}

