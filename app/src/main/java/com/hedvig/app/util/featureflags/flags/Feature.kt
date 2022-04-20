package com.hedvig.app.util.featureflags.flags

enum class Feature(
    @Suppress("unused") val explanation: String, // Used to easier get a context of what it's for.
) {
    EXTERNAL_DATA_COLLECTION("Enables external data collection for offers, from eg. Insurely"),
    FRANCE_MARKET("Used to select french market in app"),
    KEY_GEAR("Features where members can insure their important items. Only available for a small subset of members."),
    MOVING_FLOW("Lets a user change their address and get a new offer"),
    CONNECT_PAYMENT_AT_SIGN(
        "Connecting payment at sign, to avoid missing payments. Show payment step in PostOnboarding"
    ),
    UPDATE_NECESSARY(
        "Defines the lowest supported app version. Should prompt a user to update if it uses an outdated version."
    ),
    REFERRAL_CAMPAIGN("Used to show banner in referral view"),
    QUOTE_CART("Use new APIs for onboarding"),
    CONNECT_PAYIN_REMINDER("Show a reminder to connect payin on the home tab"),
    COMMON_CLAIMS("Show common claims on the home tab"),
}
