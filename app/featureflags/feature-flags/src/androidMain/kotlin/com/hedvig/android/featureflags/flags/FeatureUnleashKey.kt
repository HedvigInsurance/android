package com.hedvig.android.featureflags.flags

internal val Feature.unleashKey: String
  get() = when (this) {
    Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX -> "enable_new_conversation_from_inbox"
    Feature.UPDATE_NECESSARY -> "update_necessary"
    Feature.DISABLE_PUPPY_GUIDE -> "disable_puppy_guide"
  }
