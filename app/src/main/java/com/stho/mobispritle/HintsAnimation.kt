package com.stho.mobispritle

import android.animation.Animator
import android.os.Handler
import android.os.Looper
import android.view.View

class HintsAnimation private constructor(private val view: View) {
    private val handler = Handler(Looper.getMainLooper())
    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
    }

    fun show() {
        fadeIn()
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ fadeOut() }, FADE_OUT_TIMEOUT.toLong())
    }

    fun dismiss() {
        handler.removeCallbacksAndMessages(null)
        fadeOut()
    }

    fun hide() {
        view.visibility = View.INVISIBLE
        view.alpha = 0f
    }

    private fun fadeIn() {
        val dy = view.height
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.translationY = dy.toFloat()
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(FADE_IN_DURATION.toLong())
            .setListener(null)
    }

    private fun fadeOut() {
        view.animate()
            .alpha(0f)
            .setDuration(FADE_OUT_DURATION.toLong())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    // ignore
                }

                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(animation: Animator) {
                    // ignore
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // ignore
                }
            })
    }

    companion object {
        fun build(view: View): HintsAnimation {
            return HintsAnimation(view)
        }

        private const val FADE_IN_DURATION = 500
        private const val FADE_OUT_DURATION = 500
        private const val FADE_OUT_TIMEOUT = 13000
    }

    init {
        hide()
    }
}