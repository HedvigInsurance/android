package com.hedvig.android.featureflags.flags

internal val Feature.unleashKey: String
  get() = when (this) {
    Feature.DISABLE_CHAT -> "disable_chat"
    Feature.MOVING_FLOW -> "moving_flow"
    Feature.PAYMENT_SCREEN -> "payment_screen"
    Feature.TERMINATION_FLOW -> "disable_termination_flow"
    Feature.UPDATE_NECESSARY -> "update_necessary"
    Feature.EDIT_COINSURED -> "edit_coinsured"
    Feature.HELP_CENTER -> "disable_help_center"
    Feature.TRAVEL_ADDON -> "enable_addons"
    Feature.ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES -> "enable_video_player_in_chat_messages"
    Feature.DISABLE_REDEEM_CAMPAIGN -> "disable_redeem_campaign"
    Feature.ENABLE_CLAIM_HISTORY -> "enable_claim_history"
    Feature.PUPPY_GUIDE -> "disable_puppy_guide"
  }
