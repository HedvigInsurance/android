package com.hedvig.app.authenticate

import com.hedvig.app.feature.offer.model.QuoteCartId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeLoginStatusService(
  override var isViewingOffer: Boolean = false,
  override var isLoggedIn: Boolean = false,
) : LoginStatusService {
  override suspend fun getLoginStatus(): LoginStatus = error("Not implemented")
  override fun observeIsLoggedIn(): Flow<LoginStatus> = emptyFlow()
  override fun persistOfferIds(quoteCartId: QuoteCartId) {}
}
