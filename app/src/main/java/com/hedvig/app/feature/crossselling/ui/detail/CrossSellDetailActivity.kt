package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.crossselling.ui.CrossSell
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows

class CrossSellDetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crossSell = intent.getParcelableExtra<CrossSell>(CROSS_SELL)
            ?: throw IllegalArgumentException("Programmer error: CROSS_SELL not passed to ${this.javaClass.name}")

        window.compatSetDecorFitsSystemWindows(false)

        setContent {
            HedvigTheme {
                CrossSellDetailScreen(
                    onCtaClick = {
                        when (val action = crossSell.action) {
                            CrossSell.Action.Chat -> openChat(this)
                            is CrossSell.Action.Embark ->
                                openEmbark(this, action.embarkStoryId, crossSell.title)
                        }
                    },
                    onUpClick = { finish() },
                    data = crossSell,
                )
            }
        }
    }

    private fun openChat(context: Context) {
        val intent = ChatActivity.newInstance(context, true)
        val options =
            ActivityOptionsCompat.makeCustomAnimation(
                context,
                R.anim.chat_slide_up_in,
                R.anim.stay_in_place
            )

        ActivityCompat.startActivity(context, intent, options.toBundle())
    }

    private fun openEmbark(context: Context, embarkStoryId: String, title: String) {
        context.startActivity(
            EmbarkActivity.newInstance(context, embarkStoryId, title)
        )
    }

    companion object {
        private const val CROSS_SELL = "CROSS_SELL"
        fun newInstance(context: Context, crossSell: CrossSell) = Intent(
            context,
            CrossSellDetailActivity::class.java,
        ).apply {
            putExtra(CROSS_SELL, crossSell)
        }
    }
}
