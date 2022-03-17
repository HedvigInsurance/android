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
    ),
    QUOTE_CART(
        "quote_Cart",
        "Quote Cart APIs",
        "Use new APIs for onboarding",
        false
    ),
    HEDVIG_TYPE_FACE(
        "hedvig_type_face",
        "Use hedvig type face",
        "Replaces all text with Hedvigs own type face",
        true
    ),
    KEY_GEAR(
        "key_gear",
        "Key Gear",
        "Features where members can insure their important items. Only available for a small subset of members.",
        false
    ),
    EXTERNAL_DATA_COLLECTION(
        "external_data_collection",
        "External offer data collection",
        "Enables external data collection for offers, from eg. Insurely",
        true
    )
}
