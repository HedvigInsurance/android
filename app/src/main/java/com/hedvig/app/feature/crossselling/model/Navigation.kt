package com.hedvig.app.feature.crossselling.model

import android.content.Context
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.util.extensions.startChat

data class NavigateEmbark(val embarkStoryId: String, val embarkTitle: String) {
    fun navigate(context: Context) {
        context.startActivity(
            EmbarkActivity.newInstance(
                context = context,
                storyName = embarkStoryId,
                storyTitle = embarkTitle,
            ),
        )
    }
}

object NavigateChat {
    fun navigate(context: Context) {
        context.startChat()
    }
}
