package com.stho.mobispritle.library.algebra

import kotlin.math.*

/**
 * https://mathworld.wolfram.com/Quaternion.html
 * https://www.ashwinnarayan.com/post/how-to-integrate-quaternions/
 */
data class Quaternion(val v: Vector, val s: Double) : IRotation {
    val x: Double = v.x
    val y: Double = v.y
    val z: Double = v.z

    private val x2: Double by lazy { 2 * x * x }
    private val y2: Double by lazy { 2 * y * y }
    private val z2: Double by lazy { 2 * z * z }
    private val xy: Double by lazy { 2 * x * y }
    private val xz: Double by lazy { 2 * x * z }
    private val yz: Double by lazy { 2 * y * z }
    private val sz: Double by lazy { 2 * s * z }
    private val sy: Double by lazy { 2 * s * y }
    private val sx: Double by lazy { 2 * s * x }

    // https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
    /**
     * 1 - 2yy - 2zz
     */
    override val m11: Double by lazy { 1 - y2 - z2 }

    /**
     * 2xy - 2sz
     */
    override val m12: Double by lazy { xy - sz }

    /**
     * 2xz + 2sy
     */
    override val m13: Double by lazy { xz + sy }

    /**
     * 2xy + 2sz
     */
    override val m21: Double by lazy { xy + sz }

    /**
     * 1 - 2xx - 2zz
     */
    override val m22: Double by lazy { 1 - x2 - z2 }

    /**
     * 2yz - 2sx
     */
    override val m23: Double by lazy { yz - sx }

    /**
     * 2xz - 2sy
     */
    override val m31: Double by lazy { xz - sy }

    /**
     * 2yz + 2sx
     */
    override val m32: Double by lazy { yz + sx }

    /**
     * 1 - 2xx - 2yy
     */
    override val m33: Double by lazy { 1 - x2 - y2 }

    fun toRotationMatrix(): RotationMatrix =
        RotationMatrix(
            m11 = m11, m12 = m12, m13 = m13,
            m21 = m21, m22 = m22, m23 = m23,
            m31 = m31, m32 = m32, m33 = m33,
        )

    fun toEulerAngles(): EulerAngles =
        Rotation.getEulerAnglesFor(this)

    constructor(x: Double, y: Double, z: Double, s: Double) :
            this(v = Vector(x, y, z), s = s)

    operator fun plus(q: Quaternion): Quaternion =
            Quaternion(v + q.v, s + q.s)

    operator fun minus(q: Quaternion): Quaternion =
            Quaternion(v - q.v, s - q.s)

    operator fun times(f: Double): Quaternion =
            Quaternion(v * f, s * f)

    operator fun times(q: Quaternion): Quaternion =
            hamiltonProduct(this, q)

    operator fun div(f: Double): Quaternion =
            Quaternion(v / f, s / f)

    private fun norm(): Double =
            sqrt(normSquare())

    fun normSquare(): Double =
            x * x + y * y + z * z + s * s

    fun conjugate(): Quaternion =
            Quaternion(Vector(-x, -y, -z), s)

    fun normalize(): Quaternion =
        this * (1.0 / norm())

    companion object {

        val default: Quaternion
            get() = Quaternion(0.0, 0.0, 0.0, 1.0)

        // (r1,v1) * (r2,v2) = (r1 r2 - dot(v1,v2), r1 v2 + r2 v1 + cross(v1, v2)
        private fun hamiltonProduct(a: Quaternion, b: Quaternion): Quaternion {
            val a1 = a.s
            val b1 = a.x
            val c1 = a.y
            val d1 = a.z
            val a2 = b.s
            val b2 = b.x
            val c2 = b.y
            val d2 = b.z
            return Quaternion(
                    x = a1 * b2 + b1 * a2 + c1 * d2 - d1 * c2,
                    y = a1 * c2 + c1 * a2 - b1 * d2 + d1 * b2,
                    z = a1 * d2 + d1 * a2 + b1 * c2 - c1 * b2,
                    s = a1 * a2 - b1 * b2 - c1 * c2 - d1 * d2
            )
        }

        fun fromRotationMatrix(m: IRotation): Quaternion {
            // see: https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
            // mind, as both q and -q define the same rotation you may get q or -q, respectively
            m.run {
                when {
                    m11 + m22 + m33 > 0 -> {
                        val fourS = 2.0 * sqrt(1.0 + m11 + m22 + m33) // 4s = 4 * q.s
                        return Quaternion(
                            x = (m32 - m23) / fourS,
                            y = (m13 - m31) / fourS,
                            z = (m21 - m12) / fourS,
                            s = 0.25 * fourS
                        )
                    }
                    m11 > m22 && m11 > m33 -> {
                        val fourX = 2.0 * sqrt(1.0 + m11 - m22 - m33) // 4x = 4 * q.x
                        return Quaternion(
                            x = 0.25 * fourX,
                            y = (m12 + m21) / fourX,
                            z = (m13 + m31) / fourX,
                            s = (m32 - m23) / fourX,
                        )
                    }
                    m22 > m33 -> {
                        val fourY = 2.0 * sqrt(1.0 + m22 - m11 - m33) // 4y = 4*q.y
                        return Quaternion(
                            x = (m12 + m21) / fourY,
                            y = 0.25 * fourY,
                            z = (m23 + m32) / fourY,
                            s = (m13 - m31) / fourY
                        )
                    }
                    else -> {
                        val fourZ = 2.0 * sqrt(1.0 + m33 - m11 - m22) // 4z = 4 * q.z
                        return Quaternion(
                            x = (m13 + m31) / fourZ,
                            y = (m23 + m32) / fourZ,
                            z = 0.25 * fourZ,
                            s = (m21 - m12) / fourZ
                        )
                    }
                }
            }
        }
    }
}



