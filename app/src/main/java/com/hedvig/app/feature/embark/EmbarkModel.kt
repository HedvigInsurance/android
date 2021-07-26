package com.hedvig.app.feature.embark

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.util.Percent

data class EmbarkModel(
    val passage: EmbarkStoryQuery.Passage?,
    val navigationDirection: NavigationDirection,
    val progress: Percent,
    val isLoggedIn: Boolean,
    val hasTooltips: Boolean
)
