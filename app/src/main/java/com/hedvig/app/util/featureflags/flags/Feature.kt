package com.hedvig.app.util.featureflags.flags

enum class Feature(
    @Suppress("unused") val explanation: String, // Used to easier get a context of what it's for.
) {
    CONNECT_PAYMENT_AT_SIGN(
        "Connecting payment at sign, to avoid missing payments. Show payment step in PostOnboarding"
    ),
    EXTERNAL_DATA_COLLECTION("Enables external data collection for offers, from eg. Insurely"),
    FRANCE_MARKET("Used to select french market in app"),
    KEY_GEAR("Features where members can insure their important items. Only available for a small subset of members."),
    MOVING_FLOW("Lets a user change their address and get a new offer"),
    QUOTE_CART("Use new APIs for onboarding"),
    REFERRALS("Whether the referrals feature is enabled. Shows/Hides the forever tab. Used for Qasa."),
    REFERRAL_CAMPAIGN("Used to show banner in referral view"),
    UPDATE_NECESSARY(
        "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version."
    ),
}
