package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.ui.EmbarkActivity

fun handleAction(context: Context, action: CrossSellData.Action) {
    when (action) {
        CrossSellData.Action.Chat -> openChat(context)
        is CrossSellData.Action.Embark ->
            openEmbark(context, action.embarkStoryId, action.title)
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
