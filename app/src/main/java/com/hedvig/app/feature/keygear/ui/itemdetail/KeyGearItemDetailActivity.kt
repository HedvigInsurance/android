package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.dynamicanimation.animation.SpringAnimation
import com.hedvig.app.BASE_MARGIN_TRIPLE
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.doOnGlobalLayout
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.activity_key_gear_item_detail.*
import kotlin.math.max

// TODO: Exit animation. Should be a shared element transition to the newly created item on the tab screen
class KeyGearItemDetailActivity : BaseActivity(R.layout.activity_key_gear_item_detail) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFromCreate) {
            val (revealX, revealY) = revealCoordinates

            root.doOnGlobalLayout {
                revealPostCreateAnimation(revealX, revealY)
            }
        } else {
            postCreate.remove()
        }
    }

    private fun revealPostCreateAnimation(revealX: Int, revealY: Int) {
        val finalRadius =
            max(postCreate.width, postCreate.height).toFloat() * 1.1f

        postCreate.show()
        ViewAnimationUtils.createCircularReveal(
            postCreate,
            revealX,
            revealY,
            0f,
            finalRadius
        ).apply {
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
            doOnEnd {
                // TODO: This should be entirely driven by the AVD I think
                createdAnimation.fadeIn()

                createdLabel.show()
                createdLabel.alpha = 0f

                createdLabel.spring(SpringAnimation.TRANSLATION_Y)
                    .addUpdateListener { _, value, _ ->
                        createdLabel.alpha = 1 - (value / BASE_MARGIN_TRIPLE)
                    }
                    .animateToFinalPosition(0f)
            }
            start()
        }
    }

    companion object {
        private const val REVEAL_X = "REVEAL_X"
        private const val REVEAL_Y = "REVEAL_Y"

        private val KeyGearItemDetailActivity.isFromCreate: Boolean
            get() = intent.hasExtra(REVEAL_X) && intent.hasExtra(REVEAL_Y)

        private val KeyGearItemDetailActivity.revealCoordinates: Pair<Int, Int>
            get() = Pair(intent.getIntExtra(REVEAL_X, 0), intent.getIntExtra(REVEAL_Y, 0))

        fun newInstance(context: Context) = Intent(context, KeyGearItemDetailActivity::class.java)
        fun newInstanceFromCreate(context: Context, revealX: Int, revealY: Int) =
            Intent(context, KeyGearItemDetailActivity::class.java).apply {
                putExtra(REVEAL_X, revealX)
                putExtra(REVEAL_Y, revealY)
            }
    }
}

