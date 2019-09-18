package com.hedvig.app.util.extensions

import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

fun ImageView.avdSetLooping() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        (drawable as? AnimatedVectorDrawable)?.registerAnimationCallback(object :
            Animatable2.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                this@avdSetLooping.post {
                    avdStart()
                }
            }
        })
    } else {
        (drawable as? AnimatedVectorDrawableCompat)?.registerAnimationCallback(object :
            Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                this@avdSetLooping.post {
                    avdStart()
                }
            }
        })
    }
}

fun ImageView.avdStart() {
    (drawable as? Animatable)?.start()
}

fun ImageView.avdStop() {
    (drawable as? Animatable)?.stop()
}
