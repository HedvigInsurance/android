package com.hedvig.android.featureflags.flags

internal val Feature.unleashKey: String
  get() = when (this) {
    Feature.ALWAYS_AVAILABLE_INBOX_AND_NEW_CHAT -> "enable_new_conversation_from_inbox"
    Feature.TERMINATION_FLOW -> "disable_termination_flow"
    Feature.UPDATE_NECESSARY -> "update_necessary"
    Feature.TRAVEL_ADDON -> "enable_addons"
    Feature.ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES -> "enable_video_player_in_chat_messages"
    Feature.ENABLE_CLAIM_HISTORY -> "enable_claim_history"
    Feature.PUPPY_GUIDE -> "disable_puppy_guide"
  }
