package com.stho.mobispritle.library

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.stho.mobispritle.library.algebra.Quaternion
import com.stho.mobispritle.library.algebra.RotationMatrix
import com.stho.mobispritle.library.algebra.Vector
import com.stho.mobispritle.library.filter.FastAHRSFilter
import com.stho.mobispritle.library.filter.IOrientationFilter
import java.lang.Exception


class OrientationSensorListener(context: Context, private var filter: IOrientationFilter) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)
    private val timer = Timer()

    private var hasAccelerometer = false
    private var hasMagnetometer = false
    private var hasEstimate = false
    private var estimate: Quaternion = Quaternion.default
    private var display: Display? = null

    internal fun onResume() {
        // TODO: for API30 you shall use: context.display.rotation
        display = windowManager.defaultDisplay
        hasAccelerometer = false
        hasMagnetometer = false
        hasEstimate = false
        timer.reset()
        registerSensorListeners()
    }

    internal fun onPause() {
        removeSensorListeners()
    }

    private fun registerSensorListeners() {
        val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)

        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun removeSensorListeners() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // We don't care
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor?.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                hasMagnetometer = true
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                hasAccelerometer = true
            }
            Sensor.TYPE_GYROSCOPE -> {
                System.arraycopy(event.values, 0, gyroscopeReading, 0, gyroscopeReading.size)
                updateOrientationAnglesFromGyroscope()
            }
        }
    }

    private fun updateOrientationAnglesFromGyroscope() {
        if (!hasAccelerometer) {
            return
        }
        if (!hasMagnetometer) {
            return
        }
        if (!hasEstimate) {
            val orientation = getOrientationQuaternionFromAccelerometerMagnetometer() ?: return
            estimate = orientation
            hasEstimate = true
            timer.reset()
        }
        val dt = timer.getNextTime()
        if (dt > 0) {
            filterUpdate(dt)

            if (estimate.normSquare() > 2.0)
                throw Exception("Invalid estimate")

            filter.onOrientationAnglesChanged(estimate)
        }
    }

    /**
     * Fast AHRS Filter for Accelerometer, Magnetometer, and Gyroscope Combination with Separated Sensor Corrections
     * by Josef Justa, Vaclav Smidl, Alex Hamacek, April 2020
     */
    private fun filterUpdate(dt: Double) {
        val a: Vector = Vector.fromFloatArray(accelerometerReading).normalize()
        val m: Vector = Vector.fromFloatArray(magnetometerReading).normalize()
        val omega: Vector = Vector.fromFloatArray(gyroscopeReading)
        estimate = FastAHRSFilter.update(a, m, omega, dt, estimate)
    }

    // Compute the orientation based on the most recent readings from the device's accelerometer and magnetometer.
    private fun getOrientationQuaternionFromAccelerometerMagnetometer(): Quaternion? {
        if (!hasAccelerometer) {
            return null
        }
        if (!hasMagnetometer) {
            return null
        }
        val rotationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            return RotationMatrix.fromFloatArray(getAdjustedRotationMatrix(rotationMatrix)).toQuaternion()
        }
        return null
    }

    /*
      See the following training materials from google.
      https://codelabs.developers.google.com/codelabs/advanced-android-training-sensor-orientation/index.html?index=..%2F..advanced-android-training#0
     */
    private fun getAdjustedRotationMatrix(rotationMatrix: FloatArray): FloatArray {
        val rotationMatrixAdjusted = FloatArray(9)
        when (displayRotation) {
            Surface.ROTATION_0 -> {
                return rotationMatrix
            }
            Surface.ROTATION_90 -> {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationMatrixAdjusted)
                return rotationMatrixAdjusted
            }
            Surface.ROTATION_180 -> {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, rotationMatrixAdjusted)
                return rotationMatrixAdjusted
            }
            Surface.ROTATION_270 -> {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, rotationMatrixAdjusted)
                return rotationMatrixAdjusted
            }
        }
        return rotationMatrix
    }

    private val displayRotation: Int
        get() = display!!.rotation
}
