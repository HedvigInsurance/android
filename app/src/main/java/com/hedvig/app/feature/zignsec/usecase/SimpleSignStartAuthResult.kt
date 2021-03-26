package com.hedvig.app.feature.zignsec.usecase

sealed class SimpleSignStartAuthResult {
    data class Success(val url: String) :
        SimpleSignStartAuthResult()

    object Error : SimpleSignStartAuthResult()
}
