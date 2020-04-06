package com.hedvig.app.feature.dashboard.ui

import androidx.annotation.StringRes

data class UpsellModel(
    @get:StringRes val title: Int,
    @get:StringRes val description: Int,
    @get:StringRes val ctaText: Int
)

