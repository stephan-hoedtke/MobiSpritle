package com.stho.mobispritle.ui.home

import com.stho.mobispritle.Mode
import com.stho.mobispritle.library.algebra.Degree
import com.stho.mobispritle.library.algebra.Quaternion
import com.stho.mobispritle.library.algebra.Vector

class BubbleCalculator(quaternion: Quaternion) {

    private val g = Vector(0.0, 0.0, -1.0).rotateBy(quaternion.conjugate())

    fun getForceMode(): Mode? {
        return when {
            g.z < -THRESHOLD -> Mode.BelowParallel
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
            Mode.BelowParallel -> {
                val s = g.y * cosAlpha + g.z * sinAlpha
                val t = g.y * sinAlpha - g.z * cosAlpha
                Degree.arcTan2(s, t)
            }
            Mode.BelowOrthogonal -> {
                val s = g.x * cosAlpha + g.z * sinAlpha
                val t = g.x * sinAlpha - g.z * cosAlpha
                -Degree.arcTan2(s, t)
            }
            Mode.Above -> {
                val s = g.y * cosAlpha + g.z * sinAlpha
                val t = g.y * sinAlpha - g.z * cosAlpha
                Degree.arcTan2(s, -t)
            }
            Mode.TopDown -> {
                val t = g.x * sinAlpha + g.y * cosAlpha
                val s = g.x * cosAlpha - g.y * sinAlpha
                -Degree.arcTan2(-s, t)
            }
        }
    }

    fun getTilt(mode: Mode): Double =
        when (mode) {
            Mode.BelowParallel -> {
                val v = g.dot(Vector(0.0, 0.0, -1.0))
                -Degree.arcCos(v)
            }
            Mode.BelowOrthogonal -> {
                val v = g.dot(Vector(1.0, 0.0, -1.0))
                Degree.arcCos(v)
            }
            Mode.LandscapeNegative -> {
                val v = g.dot(Vector(0.0, 1.0, 0.0))
                90.0 - Degree.arcCos(v)
            }
            Mode.LandscapePositive -> {
                val v = g.dot(Vector(0.0, 1.0, 0.0))
                Degree.arcCos(v) - 90.0
            }
            Mode.Above -> {
                val v = g.dot(Vector(0.0, 0.0, 1.0))
                Degree.arcCos(v)
            }
            Mode.Portrait -> {
                val v = g.dot(Vector(1.0, 0.0, 0.0))
                Degree.arcCos(v) - 90.0
            }
            Mode.TopDown -> {
                val v = g.dot(Vector(1.0, 0.0, 0.0))
                90.0 - Degree.arcCos(v)
            }
        }

    companion object {
        private const val THRESHOLD = 0.8
    }
}


