package com.stho.mobispritle.library.algebra


data class RotationMatrix (
    override val m11: Double,
    override val m12: Double,
    override val m13: Double,
    override val m21: Double,
    override val m22: Double,
    override val m23: Double,
    override val m31: Double,
    override val m32: Double,
    override val m33: Double) : IRotation {

    fun toQuaternion(): Quaternion =
        Quaternion.fromRotationMatrix(this)

    companion object {

        fun fromFloatArray(m: FloatArray) =
            RotationMatrix(
                m11 = m[0].toDouble(),
                m12 = m[1].toDouble(),
                m13 = m[2].toDouble(),
                m21 = m[3].toDouble(),
                m22 = m[4].toDouble(),
                m23 = m[5].toDouble(),
                m31 = m[6].toDouble(),
                m32 = m[7].toDouble(),
                m33 = m[8].toDouble(),
            )
    }
}
