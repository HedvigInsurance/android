package com.hedvig.app.util

import com.hedvig.app.BuildConfig

enum class FeatureFlag(
    val key: String,
    val title: String,
    val explanation: String,
    val enabled: Boolean = false,
) {
    MOVING_FLOW(
        "moving_flow",
        "Moving Flow",
        "Lets a user change their address and get a new offer",
        BuildConfig.BUILD_TYPE == "debug"
    )
}
