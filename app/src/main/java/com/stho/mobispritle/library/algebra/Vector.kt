package com.stho.mobispritle.library.algebra

import kotlin.math.sqrt

data class Vector(val x: Double, val y: Double, val z: Double) {

    operator fun plus(v: Vector): Vector =
            Vector(x + v.x, y + v.y, z + v.z)

    operator fun minus(v: Vector): Vector =
            Vector(x - v.x, y - v.y, z - v.z)

    operator fun times(f: Double): Vector =
            Vector(x * f, y * f, z * f)

    operator fun div(f: Double): Vector =
            Vector(x / f, y / f, z / f)

    fun cross(v: Vector): Vector =
        cross(this, v)

    fun dot(v: Vector): Double =
        dot(this, v)

    /**
     * Rotate this vector by q: returns q # this # q* (hamilton product)
     */
    fun rotateBy(q: Quaternion): Vector =
            rotate(this, q)

    internal fun norm(): Double =
            sqrt(normSquare())

    private fun normSquare(): Double =
            x * x + y * y + z * z

    fun normalize(): Vector =
            this * (1 / norm())

    fun normalize(eps: Double): Vector =
            norm().let { if (it > eps) this.div(it) else default }


    companion object {
        val default: Vector =
                Vector(0.0, 0.0, 0.0)

        fun fromFloatArray(v: FloatArray): Vector =
                Vector(
                    x = v[0].toDouble(),
                    y = v[1].toDouble(),
                    z = v[2].toDouble(),
                )

        fun dot(a: Vector, b: Vector): Double =
                a.x * b.x + a.y * b.y + a.z * b.z

        fun cross(a: Vector, b: Vector): Vector =
                Vector(
                    x = a.y * b.z - a.z * b.y,
                    y = a.z * b.x - a.x * b.z,
                    z = a.x * b.y - a.y * b.x,
                )

        /**
         * Returns v rotate by q
         *
         *      q # (0, v) # q*
         *
         *      with q = (u, s) you use:
         *        = 2 dot(u,v) u + (s*s - dot(u,u)) v + 2 s cross(u,v)    --> 38 Operations
         *        = v + s t + cross(u,t) with t = 2 cross(u,v)            --> 30 Operations
         */
        private fun rotate(v: Vector, q: Quaternion): Vector {
            val t = cross(q.v, v) * 2.0
            return v + t * q.s + cross(q.v, t)
        }
    }
}
