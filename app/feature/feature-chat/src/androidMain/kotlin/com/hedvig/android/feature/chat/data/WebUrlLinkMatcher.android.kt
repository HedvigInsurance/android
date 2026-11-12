package com.hedvig.android.feature.chat.data

import android.util.Patterns

internal actual val webUrlLinkMatcher: Regex = Patterns.WEB_URL.toRegex()
