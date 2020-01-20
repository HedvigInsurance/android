package com.hedvig.app.feature.birthday

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.activity_birthday.*

class BirthdayActivity : AppCompatActivity(R.layout.activity_birthday) {

    private val animationEndFrame = 63

    companion object {
        private const val animationDuration = 900L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(0, 0)
        happyBirthdayText.hide()
        fromText.hide()

        //Birthday animation
        confetti_animation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                confetti_animation.hide()
                startActivity(Intent(applicationContext, LoggedInActivity::class.java))
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationStart(p0: Animator?) {


            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })

        animateText()

        confetti_animation.setMaxFrame(animationEndFrame)
        confetti_animation.playAnimation()
    }

    private fun animateText() {
        happyBirthdayText.apply {
            alpha = 0f
            show()

            animate().alpha(1f).duration = animationDuration
        }

        fromText.apply {
            alpha = 0f
            show()

            animate().alpha(1f).duration = animationDuration
        }
    }
}
