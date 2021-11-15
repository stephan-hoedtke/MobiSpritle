package com.stho.mobispritle

import android.graphics.Bitmap
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View
import com.stho.mobispritle.library.algebra.Degree
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * Drawing
 * - the outer ring (fixed)
 * - the inner ring with N, NE, E, SE, S, SW, W, NW markers (rotated by ringAngle)
 * - the north pointer (rotated by northPointerAngle)
 */
class BubbleView : View {

    interface OnRotateListener {
        fun onRotate(delta: Double)
    }

    interface OnDoubleTapListener {
        fun onDoubleTap()
    }

    private var transformation: Matrix? = null
    private var rotateListener: OnRotateListener? = null
    private var doubleTapListener: OnDoubleTapListener? = null
    private var previousAngle = 0.0
    private var bubbleAngle = 17.0
    private var ringAngle = 13.0
    private var glassAdjustmentAngle = 0.0
    private var isMirror: Boolean = false
    private var isTop: Boolean = false
    private var ring: Bitmap? = null
    private var glass: Bitmap? = null
    private var marks: Bitmap? = null
    private var bubbleTop: Bitmap? = null
    private var bubbleMiddle: Bitmap? = null
    private var gestureDetector: GestureDetector? = null

    constructor(context: Context?) : super(context) {
        setupGestureDetector()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setupGestureDetector()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setupGestureDetector()
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                this@BubbleView.onDoubleTap()
                return super.onDoubleTap(e)
            }
        })
    }

    private fun onRotate(delta: Double) {
        if (rotateListener != null) rotateListener!!.onRotate(delta)
    }

    private fun onDoubleTap() {
        if (doubleTapListener != null) doubleTapListener!!.onDoubleTap()
    }

    fun setOnRotateListener(listener: OnRotateListener?) {
        rotateListener = listener
    }

    fun setOnDoubleTapListener(listener: OnDoubleTapListener?) {
        doubleTapListener = listener
    }

    fun setBubbleAngle(value: Double) {
        if (bubbleAngle != value) {
            bubbleAngle = value
            invalidate()
        }
    }

    fun setRingAngle(value: Double) {
        if (ringAngle != value) {
            ringAngle = value
            invalidate()
        }
    }

    fun setGlassAdjustmentAngle(value: Double) {
        if (glassAdjustmentAngle != value) {
            glassAdjustmentAngle = value
            invalidate()
        }
    }

    private val glassAngle: Double
        get() = ringAngle + glassAdjustmentAngle

    fun setIsMirror(value: Boolean) {
        if (isMirror != value) {
            isMirror = value
            invalidate()
        }
    }

    fun setIsTop(value: Boolean) {
        if (isTop != value) {
            isTop = value
            invalidate()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector!!.onTouchEvent(event)
        onTouchEventRotationDragHandler(event)
        return true
    }

    private fun onTouchEventRotationDragHandler(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> previousAngle =
                getAngle(event.x, event.y)
            MotionEvent.ACTION_MOVE -> {
                val alpha = getAngle(event.x, event.y)
                val delta = ensureAngleInRange(alpha - previousAngle)
                previousAngle = alpha
                if (isMirror) {
                    onRotate(-delta)
                } else {
                    onRotate(delta)
                }
            }
        }
    }

    private fun ensureMatrix(): Matrix
        = transformation ?: onCreate(context)


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val matrix = ensureMatrix()

        /*
            Mind, all three bitmaps:
             - background
             - glass + ring + degrees + numbers
             - bubble (middle or top)
             - marks
            are of the same size in pixels: size x size (originally and after scaling)
         */
        val imageHeight = glass!!.height
        val px = (width / 2).toFloat()
        val py = (height / 2).toFloat()
        val dx = (px - (imageHeight / 2)).roundToInt().toFloat()
        val dy = (py - (imageHeight / 2)).roundToInt().toFloat()

        // ring + degrees + numbers
        matrix.setTranslate(dx, dy)
        matrix.postRotate(ringAngle.toFloat(), px, py)
        if (isMirror) {
            matrix.postScale(-1f, 1f, px, py)
        }
        canvas.drawBitmap(ring!!, matrix, null)

        // glass
        matrix.setTranslate(dx, dy)
        matrix.postRotate(glassAngle.toFloat(), px, py)
        if (isMirror) {
            matrix.postScale(-1f, 1f, px, py)
        }
        canvas.drawBitmap(glass!!, matrix, null)

        // bubble
        val radius = 3 * imageHeight
        val minMax = 4.0
        val gamma = bubbleAngle.coerceIn(-minMax, +minMax)
        val tx = (radius * Degree.sin(gamma)).toFloat()
        val ty = when {
            isTop -> (radius * (1 - Degree.cos(gamma))).toFloat()
            else -> 0f
        }
        matrix.setTranslate(dx, dy)
        matrix.postTranslate(tx, ty)
        matrix.postRotate(glassAngle.toFloat(), px, py)
        if (isMirror) {
            matrix.postScale(-1f, 1f, px, py)
        }
        when {
            isTop -> canvas.drawBitmap(bubbleTop!!, matrix, null)
            else -> canvas.drawBitmap(bubbleMiddle!!, matrix, null)
        }

        // marks
        matrix.setTranslate(dx, dy)
        matrix.postRotate(glassAngle.toFloat(), px, py)
        if (isMirror) {
            matrix.postScale(-1f, 1f, px, py)
        }
        canvas.drawBitmap(marks!!, matrix, null)
    }

    private fun onCreate(context: Context): Matrix {
        val size = width.coerceAtMost(height)
        ring = createBitmap(context, R.drawable.level_ring, size)
        glass = createBitmap(context, R.drawable.level_glass, size)
        marks = createBitmap(context, R.drawable.level_marks, size)
        bubbleTop = createBitmap(context, R.drawable.level_bubble_top, size)
        bubbleMiddle = createBitmap(context, R.drawable.level_bubble_middle, size)
        transformation = Matrix()
        return transformation!!
    }

    private fun getAngle(x: Float, y: Float): Double {
        val cx = (width shr 1).toFloat()
        val cy = (height shr 1).toFloat()
        return atan2((y - cy).toDouble(), (x - cx).toDouble()) * 180 / Math.PI + 90
    }

    companion object {

        private fun createBitmap(context: Context, resourceId: Int, size: Int): Bitmap {
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            return Bitmap.createScaledBitmap(bitmap, size, size, false)
        }

        private fun ensureAngleInRange(delta: Double): Double {
            var x = delta
            while (x > 180) {
                x -= 360.0
            }
            while (x < -180) {
                x += 360.0
            }
            return x
        }
    }
}