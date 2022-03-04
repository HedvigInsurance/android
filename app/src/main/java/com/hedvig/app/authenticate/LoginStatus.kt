package com.hedvig.app.authenticate

sealed class LoginStatus {
    object Onboarding : LoginStatus()
    object LoggedIn : LoginStatus()
    data class InOffer(
        val quoteCartId: String?,
        val quoteIds: Set<String>
    ) : LoginStatus()
}
