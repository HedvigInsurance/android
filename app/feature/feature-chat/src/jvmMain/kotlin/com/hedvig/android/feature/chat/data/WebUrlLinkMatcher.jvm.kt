package com.hedvig.android.feature.chat.data

internal actual val webUrlLinkMatcher: Regex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
