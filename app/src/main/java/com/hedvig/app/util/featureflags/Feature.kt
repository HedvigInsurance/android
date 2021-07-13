package com.hedvig.app.util.featureflags

enum class Feature(
    val key: String,
    val title: String,
    val explanation: String,
    val enabledByDefault: Boolean = false
) {
    MOVING_FLOW(
        "moving_flow",
        "Moving Flow",
        "Lets a user change their address and get a new offer"
    ),
}
