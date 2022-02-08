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
    FRANCE_MARKET(
        "france_market",
        "France Market",
        "Used to select french market in app",
        false
    ),
    ADDRESS_AUTO_COMPLETE(
        "address_auto_complete",
        "Embark component for autocompleting address",
        "Used in danish onboarding",
        false
    ),
    REFERRAL_CAMPAIGN(
        "referral_campaign",
        "Referral Campaign",
        "Used to show banner in referral view",
        false
    )
}
