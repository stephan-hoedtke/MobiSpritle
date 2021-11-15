package com.stho.mobispritle;

import android.animation.Animator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class ViewAnimation {
    private final View view;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static ViewAnimation build(View view) {
        return new ViewAnimation(view);
    }

    public void cleanup() {
        handler.removeCallbacksAndMessages(null);
    }

    private ViewAnimation(View view) {
        this.view = view;
        hide();
    }

    public void show() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::fadeIn, FADE_IN_DELAY);
    }

    public void dismiss() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::fadeOut, FADE_OUT_DELAY);
    }

    public void hide() {
        view.setVisibility(View.INVISIBLE);
        view.setAlpha(0f);
    }

    private void fadeIn() {
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(FADE_IN_DURATION)
                .setListener(null);

    }

    private void fadeOut() {
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(0f)
                .setDuration(FADE_OUT_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // ignore
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // ignore
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // ignore
                    }
                });
    }

    private static final int FADE_IN_DELAY = 10;
    private static final int FADE_IN_DURATION = 500;
    private static final int FADE_OUT_DELAY = 300;
    private static final int FADE_OUT_DURATION = 500;

}
