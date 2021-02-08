package com.hedvig.app.feature.ratings

import com.hedvig.app.util.jsonObjectOf
import com.mixpanel.android.mpmetrics.MixpanelAPI

class RatingsTracker(
    private val mixpanel: MixpanelAPI
) {
    fun doNotLikeApp() = mixpanel.track(
        "RATINGS_DIALOG_NO",
        jsonObjectOf(
            "question" to "RATINGS_DIALOG_BODY"
        )
    )

    fun likeApp() = mixpanel.track(
        "RATINGS_DIALOG_YES",
        jsonObjectOf(
            "question" to "RATINGS_DIALOG_BODY"
        )
    )

    fun noToFeedback() = mixpanel.track(
        "RATINGS_DIALOG_NO",
        jsonObjectOf(
            "question" to "RATINGS_DIALOG_BODY_FEEDBACK"
        )
    )

    fun yesToFeedback() = mixpanel.track(
        "RATINGS_DIALOG_YES",
        jsonObjectOf(
            "question" to "RATINGS_DIALOG_BODY_FEEDBACK"
        )
    )

    fun rate() = mixpanel.track("RATINGS_DIALOG_BODY_RATE_YES")
    fun doNotRate() = mixpanel.track("RATINGS_DIALOG_BODY_RATE_NO")
}
