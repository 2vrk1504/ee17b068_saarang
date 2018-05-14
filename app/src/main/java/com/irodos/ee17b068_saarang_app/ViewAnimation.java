package com.irodos.ee17b068_saarang_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

/**
 * Created by Vallabh on 4/29/2018.
 */
public class ViewAnimation{
    View v;
    public static final int ANIMATION_SHOW = 1;
    public static final int ANIMATION_HIDE= 2;

    public ViewAnimation(View v){
        this.v = v;
    }

    public void circleAnimation(int type) {
        if(type == ANIMATION_SHOW) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int cx = v.getWidth() / 2;
                int cy = v.getHeight() / 2;
                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);
                // create the animator for this view (the start radius is zero)
                Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
                // make the view visible and start the animation
                v.setVisibility(View.VISIBLE);
                anim.start();
            } else
                v.setVisibility(View.VISIBLE);
        }
        else if(type == ANIMATION_HIDE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = v.getWidth() / 2;
            int cy = v.getHeight() / 2;
            float initialRadius = (float) Math.hypot(cx, cy);
            Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, initialRadius, 0);

            //Waiting till Animation ends
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(View.INVISIBLE);
                }
            });
            anim.start();
        } else
            v.setVisibility(View.INVISIBLE);
        }
    }

}

