package com.stho.mobispritle.library.algebra

import kotlin.math.*

object Rotation {

    /**
     * Returns the rotation matrix for given Euler angles rotating a vector in sensor frame to earth frame
     *      v_earth = q # v_sensor # q*
     */
    fun eulerAnglesToQuaternion(omega: EulerAngles): Quaternion {
        val cosX = cos(omega.x / 2)
        val sinX = sin(omega.x / 2)
        val cosY = cos(omega.y / 2)
        val sinY = sin(omega.y / 2)
        val cosZ = cos(omega.z / 2)
        val sinZ = sin(omega.z / 2)

        // calculate as q.inverse() of q = qy * qx * qz with
        //  qx = Quaternion(s = cos(X/2), v = sin(X/2) * i) for pitch
        //  qy = Quaternion(s = cos(Y/2), v = sin(Y/2) * j) for roll
        //  qz = Quaternion(s = cos(Z/2), v = sin(Z/2) * k) for azimuth

        return Quaternion(
            s = cosY * cosX * cosZ + sinY * sinX * sinZ,
            x = cosY * sinX * cosZ + sinY * cosX * sinZ,
            y = sinY * cosX * cosZ - cosY * sinX * sinZ,
            z = cosY * cosX * sinZ - sinY * sinX * cosZ,
        ).conjugate()
    }

    /**
     * Returns the orientation (azimuth, pitch, roll) of the device
     *
     *      (A) for the normal case:
     *                  cos(x) <> 0:
     *
     *         -->      m12 = cosX * sinZ,
     *                  m22 = cosX * cosZ,
     *                  m31 = cosX * sinY,
     *                  m32 = -sinX,
     *                  m33 = cosX * cosY
     *
     *          -->     X = asin(-m32)
     *                  Y = atan(m31 / m33)
     *                  Z = atan(m12 / m22)
     *
     *      (B) for the gimbal lock when:
     *                  cos(x) = 0
     *                  sin(x) = +/-1 (X=+/-90°)
     *
     *              m11 = sinX * sinY * sinZ + cosY * cosZ,
     *              m13 = sinX * cosY * sinZ - sinY * cosZ,
     *              m21 = sinX * sinY * cosZ - cosY * sinZ,
     *              m23 = sinX * cosY * cosZ + sinY * sinZ,
     *
     *
     *          --> only (Y - Z) is defined, as cosY * cosZ + sinY * sinZ = cos(Y - Z) etc.
     *          --> assume z = 0
     *                  cos(z) = 1
     *                  sin(z) = 0
     *
     *          (B.1) when sin(X) = -1 (X=-90°):
     *                  m21 = - sinY * cosZ - cosY * sinZ = - sin(Y + Z)
     *                  m23 = - cosY * cosZ + sinY * sinZ = - cos(Y + Z)
     *
     *          -->     Y = atan2(-m21, -m23) and Y = Y' + Z' for any other Y' and Z'
     *
     *          (B.2) when sin(X) = -1 (X=-90°)
     *                  m21 = sinY * cosZ - cosY * sinZ = sin(Y - Z) = sinY
     *                  m23 = cosY * cosZ + sinY * sinZ = cos(Y - Z) = cosY
     *
     *          -->     Y = atan2(m21, m23) and Y = Y' - Z' for any other Y' and Z'
     *
     * see also: SensorManager.getOrientation()
     */
    internal fun getEulerAnglesFor(r: IRotation): EulerAngles =
        //
        // SensorManager.getOrientation()
        //            values[0] = (float) Math.atan2(R[1], R[4]);   // azimuth: atan2(m12, m22)
        //            values[1] = (float) Math.asin(-R[7]);         // pitch: asin(-m32)
        //            values[2] = (float) Math.atan2(-R[6], R[8]);  // roll: atan2(-m31, m33)
        //
        if (isGimbalLockForSinus(r.m32)) {
            if (r.m32 < 0) { // pitch 90°
                EulerAngles(
                    x = PI_OVER_TWO,
                    y = atan2(r.m21, r.m23),
                    z = 0.0,
                )
            } else { // pitch -90°
                EulerAngles(
                    x = -PI_OVER_TWO,
                    y = atan2(-r.m21, -r.m23),
                    z = 0.0,
                )
            }
        } else {
            EulerAngles(
                x = asin(-r.m32),
                y = atan2(r.m31, r.m33),
                z = atan2(r.m12, r.m22),
            )
        }

    /**
     * Returns if sin(x) is about +/- 1.0
     */
    private fun isGimbalLockForSinus(sinX: Double): Boolean =
        sinX < GIMBAL_LOCK_SINUS_MINIMUM || sinX > GIMBAL_LOCK_SINUS_MAXIMUM

    private const val PI_OVER_TWO = PI / 2
    private const val GIMBAL_LOCK_SINUS_MINIMUM: Double = -0.999999
    private const val GIMBAL_LOCK_SINUS_MAXIMUM: Double = 0.999999
}