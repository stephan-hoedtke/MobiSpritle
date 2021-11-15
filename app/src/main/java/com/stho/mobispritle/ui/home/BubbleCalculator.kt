package com.stho.mobispritle.ui.home

import com.stho.mobispritle.Mode
import com.stho.mobispritle.library.algebra.Quaternion
import com.stho.mobispritle.library.algebra.Vector
import com.stho.myorientation.library.algebra.Degree
import kotlin.math.abs

class BubbleCalculator(quaternion: Quaternion) {

    val g = Vector(0.0, 0.0, -1.0).rotateBy(quaternion.conjugate())

    fun getForceMode(): Mode? {
        val x2 by lazy { g.x * g.x }
        val y2 by lazy { g.y * g.y }
        val z2 by lazy { g.z * g.z }
        return when {
            g.z < -THRESHOLD -> Mode.Below
            g.z > +THRESHOLD -> Mode.Above
            g.x < -THRESHOLD -> Mode.LandscapeNegative
            g.x > +THRESHOLD -> Mode.LandscapePositive
            g.y < -THRESHOLD -> Mode.Portrait
            g.y > +THRESHOLD -> Mode.TopDown
            else -> null
        }
    }

    /**
     * Returns the gamma angle in degree for a level angle in degree
     *
     * Calculation:
     *      for Portrait:
     *          a = Vector(x = cos(alpha), y = -sin(alpha), z = 0)
     *          b = Vector(x = -sin(alpha), y = -cos(alpha), z = 0)
     *          c = Vector(0, 0, -1)
     *
     *          g = s * a + t * b + r * c
     *
     *          gx = s * cos(alpha) - t * sin(alpha)
     *          gy = -s * cos(alpha) - t * cos(alpha)
     *          gz = -r
     *
     *      --> s = gx * cos(alpha) - gy * sin(alpha)
     *         -t = gx * sin(alpha) + gy * cos(alpha)
     *         -r = gz
     *
     *          tan(gamma) = s / t
     */
    fun getGamma(mode: Mode, alpha: Double): Double {
        val sinAlpha = Degree.sin(alpha)
        val cosAlpha = Degree.cos(alpha)
        return when (mode) {
            Mode.Portrait -> {
                val t = g.x * sinAlpha + g.y * cosAlpha
                val s = g.x * cosAlpha - g.y * sinAlpha
                -Degree.arcTan2(s, -t)
            }
            Mode.LandscapePositive -> {
                val s = g.x * sinAlpha + g.y * cosAlpha
                val t = g.x * cosAlpha - g.y * sinAlpha
                -Degree.arcTan2(s, t)
            }
            Mode.LandscapeNegative -> {
                val s = g.x * sinAlpha + g.y * cosAlpha
                val t = g.x * cosAlpha - g.y * sinAlpha
                -Degree.arcTan2(-s, -t)
            }
            Mode.Below -> {
                val s = g.x * sinAlpha + g.y * cosAlpha
                val r = -g.z
                -Degree.arcTan2(s, r)
            }
            Mode.Above -> {
                val s = g.x * sinAlpha + g.y * cosAlpha
                val r = g.z
                -Degree.arcTan2(-s, r)
            }
            Mode.TopDown -> {
                val t = g.x * sinAlpha + g.y * cosAlpha
                val s = g.x * cosAlpha - g.y * sinAlpha
                -Degree.arcTan2(-s, t)
            }
        }
    }

    companion object {
        private const val THRESHOLD = 0.8
    }
}


