package com.stho.mobispritle.library.filter

import com.stho.mobispritle.library.algebra.Quaternion
import com.stho.mobispritle.library.algebra.RotationMatrix
import com.stho.mobispritle.library.algebra.Vector
import kotlin.math.acos
import kotlin.math.sqrt

object FastAHRSFilter {

    /**
     * Update the current estimation by a correction derived from the normalized sensor accelerometer and magnetometer readings a and m
     */
    fun update(a: Vector, m: Vector, omega: Vector, dt: Double, estimate: Quaternion): Quaternion {

        // Get updated Gyro delta rotation from gyroscope readings
        val deltaRotation: Quaternion = getDeltaRotationFromGyroFSCF(omega, dt)

        // prediction := estimation rotated by gyroscope readings
        val prediction: Quaternion = estimate.times(deltaRotation)
        val matrix: RotationMatrix = prediction.toRotationMatrix()

        // prediction of a := Vector(0.0, 0.0, 1.0).rotateBy(prediction.inverse()) --> normalized
        val aPrediction = Vector(
            matrix.m31,
            matrix.m32,
            matrix.m33
        )

        // reference direction of magnetic field in earth frame after distortion compensation
        val b: Vector = flux(aPrediction, m)

        // prediction of m := Vector(0.0, b.y, b.z).rotateBy(prediction.inverse()) --> normalized
        val mPrediction = Vector(
            matrix.m21 * b.y + matrix.m31 * b.z,
            matrix.m22 * b.y + matrix.m32 * b.z,
            matrix.m23 * b.y + matrix.m33 * b.z
        )

        // Calculate the required correction:
        // - the magnitude as "angle between prediction and measurement" := arcCos(measurement dot prediction)
        // - the direction as unit vector := (measurement x prediction).normalize()
        val aAlpha = angleBetweenUnitVectors(a, aPrediction)
        val aCorrection: Vector = a.cross(aPrediction).normalize()
        val mAlpha = angleBetweenUnitVectors(m, mPrediction)
        val mCorrection: Vector = m.cross(mPrediction).normalize(EPS)

        // Limit the magnitude of correction with time factor
        val aBeta = dt.coerceAtMost(1.0) * f(aAlpha)
        val mBeta = dt.coerceAtMost(1.0) * f(mAlpha)

        // Calculate fused correction and the new estimate
        val fCorrection: Vector = getFusedCorrectionFSCF(aCorrection, aBeta, mCorrection, mBeta)
        val fNorm: Double = fCorrection.norm()
        return if (fNorm > EPS) {
            // new estimate := prediction rotate by fused correction from acceleration and magnetometer
            val qCorrection = Quaternion(fCorrection.x, fCorrection.y, fCorrection.z, 1.0)
            prediction.times(qCorrection).normalize()
        } else {
            // new estimate := prediction
            prediction.normalize()
        }
    }

    /**
     * The rotation is not exactly the same, but similar to the "default approach":
     * theta = ||omega|| * dt
     * Q = Q(s = cos(theta/2), v = sin(theta2) * |omega|)
     *
     * @param omega angle velocity around x, y, z, in radians/second
     * @param dt time period in seconds
     */
    private fun getDeltaRotationFromGyroFSCF(omega: Vector, dt: Double): Quaternion {
        val dx: Double = omega.x * dt
        val dy: Double = omega.y * dt
        val dz: Double = omega.z * dt
        return Quaternion(dx / 2, dy / 2, dz / 2, 1.0)
    }

    private fun angleBetweenUnitVectors(a: Vector, b: Vector): Double {
        val c: Double = a.dot(b)
        return if (-1.0 <= c && c <= 1.0) acos(c) else 0.0
    }

    /**
     * Default:
     * f(x) := lambda * x
     * with lambda = 0.5 for small x
     * Non-linear:
     * f(x) := lambda * x + tau * x^2
     * with f(x) = lambda * x for small x
     * and f(x) = 10 * lambda * x for x about 20°
     *
     * 20° --> x = 2 PI / 360° * 20° = PI/9
     * f'(0) = lambda
     * f(0) = 0
     * f(PI/9) = 10 * lambda * PI/9 = lambda * PI/9 + tau * (PI/9)^2
     * tau = lambda * 81 / PI
     */
    private fun f(alpha: Double): Double {
        return (alpha * (LAMBDA1 + alpha * TAU1)).coerceAtMost(LAMBDA2)
    }

    /**
     * Returns the magnetic field in earth frame after distortion correction.
     *
     * Note:
     * Normalization is required as the input vectors may not be normalized and the calculation may fail badly otherwise.
     */
    private fun flux(a: Vector, m: Vector): Vector {
        val bz: Double = (a.x * m.x + a.y * m.y + a.z * m.z) / (a.norm() * m.norm())
        val by = sqrt(1 - bz * bz)
        return Vector(0.0, by, bz)
    }

    private fun getFusedCorrectionFSCF(a: Vector, aBeta: Double, m: Vector, mBeta: Double): Vector {
        val fa = aBeta / 2
        val fm = mBeta / 2
        return Vector(
            a.x * fa + m.x * fm,
            a.y * fa + m.y * fm,
            a.z * fa + m.z * fm
        )
    }

    private const val EPS = 1E-9
    private const val LAMBDA1 = 1.0
    private const val LAMBDA2 = 1.0
    private const val TAU1 = 81 / Math.PI * LAMBDA1
}

