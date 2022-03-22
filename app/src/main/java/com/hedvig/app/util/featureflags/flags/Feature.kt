package com.hedvig.app.util.featureflags.flags

enum class Feature(
    val key: String,
    val title: String,
    val explanation: String,
) {
    EXTERNAL_DATA_COLLECTION(
        "external_data_collection",
        "External offer data collection",
        "Enables external data collection for offers, from eg. Insurely",
    ),
    FRANCE_MARKET(
        "france_market",
        "France Market",
        "Used to select french market in app",
    ),
    KEY_GEAR(
        "key_gear",
        "Key Gear",
        "Features where members can insure their important items. Only available for a small subset of members.",
    ),
    MOVING_FLOW(
        "moving_flow",
        "Moving Flow",
        "Lets a user change their address and get a new offer"
    ),
    CONNECT_PAYMENT_AT_SIGN(
        "post_onboarding_show_payment_step",
        "Post onboarding show payment step",
        "Connecting payment at sign, to avoid missing payments. Show payment step in PostOnboarding"
    ),
    UPDATE_NECESSARY(
        "update_necessary",
        "Update necessary",
        "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version."
    ),
    REFERRAL_CAMPAIGN(
        "referral_campaign",
        "Referral Campaign",
        "Used to show banner in referral view",
    ),
    QUOTE_CART(
        "quote_Cart",
        "Quote Cart APIs",
        "Use new APIs for onboarding",
    ),
}
