package com.stho.mobispritle

import com.stho.mobispritle.library.algebra.RotationMatrix
import com.stho.mobispritle.library.algebra.Vector
import com.stho.mobispritle.ui.home.BubbleCalculator
import com.stho.myorientation.library.algebra.EulerAngles
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BubbleCalculatorUnitTest {


    @Test
    fun getMatrix_isCorrect() {
        getMatrix_isCorrect(0.0, 0.0, 0.0)
        getMatrix_isCorrect(10.0, 20.0, 30.0)
    }

    private fun getMatrix_isCorrect(azimuth: Double, pitch: Double, roll: Double) {
        val eulerAngles = EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)
        val matrix = eulerAngles.toRotationMatrix()
        val value = matrix.toEulerAngles()
        Assert.assertEquals("Azimuth expected=$azimuth actual=${value.azimuth}", azimuth, value.azimuth, EPS)
        Assert.assertEquals("Pitch expected=$pitch actual=${value.pitch}", pitch, value.pitch, EPS)
        Assert.assertEquals("Roll expected=$roll actual=${value.roll}", roll, value.roll, EPS)

    }

    @Test
    fun mode_isCorrect() {
        mode_isCorrect(azimuth = 0.0, pitch = 0.0, roll = 0.0, expectedMode = BubbleCalculator.Mode.Below)
        mode_isCorrect(azimuth = 10.0, pitch = 0.0, roll = 0.0, expectedMode = BubbleCalculator.Mode.Below)
        mode_isCorrect(azimuth = 10.0, pitch = 20.0, roll = 30.0, expectedMode = BubbleCalculator.Mode.Below)
        mode_isCorrect(azimuth = 10.0, pitch = 200.0, roll = 10.0, expectedMode = BubbleCalculator.Mode.Above)
        mode_isCorrect(azimuth = 90.0, pitch = 80.0, roll = 80.0, expectedMode = BubbleCalculator.Mode.LookFromTheSide)
        mode_isCorrect(azimuth = 90.0, pitch = 10.0, roll = 80.0, expectedMode = BubbleCalculator.Mode.LookFromTheSide)
    }

    private fun mode_isCorrect(azimuth: Double, pitch: Double, roll: Double, expectedMode: BubbleCalculator.Mode) {
        val eulerAngles = EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)
        val matrix = eulerAngles.toRotationMatrix()
        val calculator = BubbleCalculator(matrix.toQuaternion())
        val actualMode = calculator.mode
        Assert.assertEquals("Mode expected=$expectedMode actual=$actualMode", expectedMode, actualMode)
    }

    @Test
    fun gamma_isCorrect() {
        gamma_isCorrect(alpha = 0.0, azimuth = 0.0, pitch = 0.0, roll = 0.0, expectedGammaValue = 0.0)
    }

    private fun gamma_isCorrect(alpha: Double, azimuth: Double, pitch: Double, roll: Double, expectedGammaValue: Double) {
        val eulerAngles = EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)
        val matrix = eulerAngles.toRotationMatrix()
        val calculator = BubbleCalculator(matrix.toQuaternion())
        val actualGammaValue = calculator.getGamma(alpha)
        Assert.assertEquals("$alpha expected=$expectedGammaValue actual=$actualGammaValue", expectedGammaValue, actualGammaValue, EPS)
    }

    private fun getMatrix(g: Vector): RotationMatrix {
        val n = g.normalize()
        return RotationMatrix(
            m11 = 1.0, m12 = 0.0, m13 = -n.x,
            m21 = 0.0, m22 = 1.0, m23 = -n.y,
            m31 = n.x, m32 = n.y, m33 = -n.z,
        )
    }

    companion object {
        private const val EPS: Double = 1E-8
    }
}