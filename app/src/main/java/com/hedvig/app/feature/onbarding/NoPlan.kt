package com.hedvig.app.feature.onbarding

enum class NoPlan {
    BUNDLE,
    CONTENT,
    TRAVEL;

    fun getEmbarkPath() = when (this) {
        BUNDLE -> "combo"
        CONTENT -> "contents"
        TRAVEL -> "travel"
    }
}
