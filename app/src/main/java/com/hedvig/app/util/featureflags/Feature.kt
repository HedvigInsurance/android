package com.hedvig.app.util.featureflags

enum class Feature(
    val key: String,
    val title: String,
    val explanation: String,
    val enabledByDefault: Boolean = false,
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
    CONNECT_PAYMENT_AT_SIGN(
        "CONNECT_PAYMENT_AT_SIGN",
        "Connect payment at sign",
        "Connecting payment at sign, to avoid missing payments",
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
