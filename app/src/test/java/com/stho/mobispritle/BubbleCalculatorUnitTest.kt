package com.stho.mobispritle

import com.stho.mobispritle.library.algebra.Degree
import com.stho.mobispritle.library.algebra.EulerAngles
import com.stho.mobispritle.ui.home.BubbleCalculator
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BubbleCalculatorUnitTest {


    @Test
    fun eulerAngles_areCorrect() {
        eulerAngles_areCorrect(0.0, 0.0, 0.0)
        eulerAngles_areCorrect(10.0, 20.0, 30.0)
    }

    private fun eulerAngles_areCorrect(azimuth: Double, pitch: Double, roll: Double) {
        val eulerAngles = EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)
        val quaternion = eulerAngles.toQuaternion()
        val value = quaternion.toEulerAngles()
        Assert.assertEquals("Azimuth expected=$azimuth actual=${value.azimuth}", azimuth, value.azimuth, EPS)
        Assert.assertEquals("Pitch expected=$pitch actual=${value.pitch}", pitch, value.pitch, EPS)
        Assert.assertEquals("Roll expected=$roll actual=${value.roll}", roll, value.roll, EPS)

    }

    @Test
    fun getForceMode_isCorrect() {
        getForceMode_isCorrect(azimuth = 0.0, pitch = 0.0, roll = 0.0, expectedMode = Mode.BelowParallel)
        getForceMode_isCorrect(azimuth = 2.0, pitch = 2.0, roll = 2.0, expectedMode = Mode.BelowParallel)
        getForceMode_isCorrect(azimuth = 60.0, pitch = 2.0, roll = -88.0, expectedMode = Mode.LandscapePositive)
        getForceMode_isCorrect(azimuth = -60.0, pitch = 2.0, roll = 88.0, expectedMode = Mode.LandscapeNegative)
        getForceMode_isCorrect(azimuth = 2.0, pitch = 178.0, roll = -2.0, expectedMode = Mode.Above)
        getForceMode_isCorrect(azimuth = 2.0, pitch = -88.0, roll = 2.0, expectedMode = Mode.Portrait)
        getForceMode_isCorrect(azimuth = 88.0, pitch = 88.0, roll = 88.0, expectedMode = Mode.TopDown)
    }

    private fun getForceMode_isCorrect(azimuth: Double, pitch: Double, roll: Double, expectedMode: Mode) {
        val eulerAngles = EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)
        val calculator = BubbleCalculator(eulerAngles.toQuaternion())
        val actualMode = calculator.getForceMode()
        Assert.assertEquals("Mode expected=$expectedMode actual=$actualMode", expectedMode, actualMode)
    }

    @Test
    fun gamma_isCorrect() {
        // mind:
        //      azimuth moves the top leftwards (x < 0)
        //      pitch moves the top downwards (z < 0)
        //      roll moves the left side upwards (y > 0)

        gamma_isCorrect(Mode.BelowParallel, alpha = 0.0, azimuth = 0.0, pitch = 0.0, roll = 0.0, expectedGammaValue = 0.0)
        gamma_isCorrect(Mode.BelowParallel, alpha = 0.0, azimuth = 0.0, pitch = -1.0, roll = 0.0, expectedGammaValue = -1.0)
        gamma_isCorrect(Mode.BelowParallel, alpha = 2.0, azimuth = 0.0, pitch = -1.0, roll = 0.0, expectedGammaValue = -3.0)
        gamma_isCorrect(Mode.BelowParallel, alpha = -4.0, azimuth = 10.0, pitch = -2.0, roll = 0.0, expectedGammaValue = 2.0)

        gamma_isCorrect(Mode.Portrait, alpha = 0.0, azimuth = 0.0, pitch = -90.0, roll = 0.0, expectedGammaValue = 0.0)
        gamma_isCorrect(Mode.Portrait, alpha = 0.0, azimuth = 90.0, pitch = -89.0, roll = 90.0, expectedGammaValue = 1.0)
        gamma_isCorrect(Mode.Portrait, alpha = 1.0, azimuth = 90.0, pitch = -89.0, roll = 90.0, expectedGammaValue = 0.0)
        gamma_isCorrect(Mode.Portrait, alpha = 4.0, azimuth = 90.0, pitch = -89.0, roll = 90.0, expectedGammaValue = -3.0)

        gamma_isCorrect(Mode.LandscapeNegative, alpha = 0.0, azimuth = -90.0, pitch = 0.0, roll = 90.0, expectedGammaValue = 0.0)
        gamma_isCorrect(Mode.LandscapeNegative, alpha = 0.0, azimuth = -90.0, pitch = -1.0, roll = 90.0, expectedGammaValue = -1.0)
        gamma_isCorrect(Mode.LandscapeNegative, alpha = -1.0, azimuth = -90.0, pitch = -1.0, roll = 90.0, expectedGammaValue = 0.0)
        gamma_isCorrect(Mode.LandscapeNegative, alpha = -4.0, azimuth = -90.0, pitch = -1.0, roll = 90.0, expectedGammaValue = 3.0)
    }

    private fun cosSin(alpha: Double, beta: Double): Double =
        -Degree.arcTan2(Degree.cos(alpha) * Degree.sin(beta), Degree.cos(beta))

    private fun gamma_isCorrect(mode: Mode, alpha: Double, azimuth: Double, pitch: Double, roll: Double, expectedGammaValue: Double) {
        val eulerAngles = EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)
        val calculator = BubbleCalculator(eulerAngles.toQuaternion())
        val actualGammaValue = calculator.getGamma(mode, alpha)
        val actualMode = calculator.getForceMode()
        Assert.assertEquals("$alpha expected=$expectedGammaValue actual=$actualGammaValue", expectedGammaValue, actualGammaValue, EPS)
        Assert.assertEquals("$alpha expected=$mode actual=$actualMode", mode, actualMode)
    }

    companion object {
        private const val EPS: Double = 1E-8
    }
}