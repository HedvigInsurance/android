package com.hedvig.app.feature.ratings

import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.jsonObjectOf

class RatingsTracker(
    private val trackingFacade: TrackingFacade
) {
    fun doNotLikeApp() = trackingFacade.track(
        "RATINGS_DIALOG_NO",
        jsonObjectOf(
            "question" to "RATINGS_DIALOG_BODY"
        )
    )

    fun likeApp() = trackingFacade.track(
        "RATINGS_DIALOG_YES",
        jsonObjectOf(
            "question" to "RATINGS_DIALOG_BODY"
        )
    )

    fun noToFeedback() = trackingFacade.track(
        "RATINGS_DIALOG_NO",
        jsonObjectOf(
            "question" to "RATINGS_DIALOG_BODY_FEEDBACK"
        )
    )

    fun yesToFeedback() = trackingFacade.track(
        "RATINGS_DIALOG_YES",
        jsonObjectOf(
            "question" to "RATINGS_DIALOG_BODY_FEEDBACK"
        )
    )

    fun rate() = trackingFacade.track("RATINGS_DIALOG_BODY_RATE_YES")
    fun doNotRate() = trackingFacade.track("RATINGS_DIALOG_BODY_RATE_NO")
}
