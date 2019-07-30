package com.hedvig.app.util.extensions

import android.animation.Animator

// Reimplement doOnEnd from core-ktx as we cannot do AndroidX basically until we ditch React Native
inline fun Animator.doOnEnd(crossinline action: (animator: Animator) -> Unit) =
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator) {
        }

        override fun onAnimationCancel(p0: Animator) {
        }

        override fun onAnimationStart(p0: Animator) {
        }

        override fun onAnimationEnd(p0: Animator) {
            action(p0)
        }
    })
