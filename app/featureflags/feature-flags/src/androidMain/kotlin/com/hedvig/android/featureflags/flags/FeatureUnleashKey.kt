package com.hedvig.android.featureflags.flags

internal val Feature.unleashKey: String
  get() = when (this) {
    Feature.DISABLE_PUPPY_GUIDE -> "disable_puppy_guide"
    Feature.ENABLE_CLAIM_INTENT_RESUME -> "enable_claim_intent_resume"
    Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX -> "enable_new_conversation_from_inbox"
    Feature.UPDATE_NECESSARY -> "update_necessary"
  }
