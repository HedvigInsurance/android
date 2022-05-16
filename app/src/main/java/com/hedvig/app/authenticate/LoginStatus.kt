package com.hedvig.app.authenticate

import com.hedvig.app.feature.offer.model.QuoteCartId

sealed class LoginStatus {
    object Onboarding : LoginStatus()
    object LoggedIn : LoginStatus()
    data class InOffer(val quoteCartId: QuoteCartId) : LoginStatus()
}
