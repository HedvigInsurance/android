package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.extensions.startChat

fun handleAction(
    context: Context,
    action: CrossSellData.Action,
    trackingFacade: TrackingFacade
) {
    when (action) {
        CrossSellData.Action.Chat -> context.startChat(trackingFacade)
        is CrossSellData.Action.Embark ->
            openEmbark(context, action.embarkStoryId, action.title)
    }
}

private fun openEmbark(context: Context, embarkStoryId: String, title: String) {
    context.startActivity(
        EmbarkActivity.newInstance(context, embarkStoryId, title)
    )
}
