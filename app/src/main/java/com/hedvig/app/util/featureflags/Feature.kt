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
    INSURELY_EMBARK(
        "insurely_embark",
        "Insurely for Embark",
        "Fetch price information about a members previous insurance provider in embark after selecting an insurer in " +
            "PreviousInsurerBottomSheet",
        false
    ),
    EMBARK_CLAIMS(
        "embark_claims",
        "Embark claims",
        "Use embark for claims and use new audio recorder action, instead of opening the chat",
        false
    ),
    CLAIMS_STATUS(
        "claims_status",
        "Claims Status",
        "Show the status of claims on the home screen as a carousel",
        false,
    ),
    FRANCE_MARKET(
        "france_market",
        "France Market",
        "Used to select french market in app",
        false
    ),
    SE_EMBARK_ONBOARDING(
        "se_embark_onboarding",
        "Embark onboarding for Swedish market",
        "Will replace bot service",
        false
    ),
    CONNECT_PAYMENT_AT_SIGN(
        "CONNECT_PAYMENT_AT_SIGN",
        "Connect payment at sign",
        "Connect payment at sign (offer screen) for SimpleSign - instead of after signing. NO and DK affected.",
        false
    )
}
