package com.hedvig.app.util.featureflags

enum class Feature(
    val key: String,
    val title: String,
    val explanation: String,
    val enabledByDefault: Boolean = false
) {
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
    CONNECT_PAYMENT_AT_SIGN(
        "CONNECT_PAYMENT_AT_SIGN",
        "Connect payment at sign",
        "Connecting payment at sign, to avoid missing payments",
        true
    )
}
