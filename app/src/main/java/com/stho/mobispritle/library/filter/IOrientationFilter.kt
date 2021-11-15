package com.stho.mobispritle.library.filter

import androidx.lifecycle.LiveData
import com.stho.mobispritle.library.algebra.Quaternion

interface IOrientationFilter {
    val quaternionLD: LiveData<Quaternion>
    fun onOrientationAnglesChanged(newOrientation: Quaternion)
}
