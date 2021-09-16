package com.hedvig.app.feature.offer

import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.feature.offer.quotedetail.buildDocuments
import com.hedvig.app.feature.offer.quotedetail.buildInsurableLimits
import com.hedvig.app.feature.offer.quotedetail.buildPerils
import com.hedvig.app.feature.offer.ui.checkout.CheckoutParameter
import com.hedvig.app.feature.offer.ui.checkoutTextRes
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

class MockOfferViewModel : OfferViewModel() {
    init {
        load()
    }

    override fun removeDiscount() = Unit
    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = Unit
    override suspend fun triggerOpenChat() = Unit

    override fun onOpenQuoteDetails(
        id: String,
    ) {
        val quote = mockData.quoteBundle.quotes.first { it.id == id }
        _events.tryEmit(
            Event.OpenQuoteDetails(
                QuoteDetailItems(
                    quote.displayName,
                    buildPerils(quote),
                    buildInsurableLimits(quote),
                    buildDocuments(quote),
                )
            )
        )
    }

    override fun approveOffer() {
        _events.tryEmit(Event.ApproveSuccessful(LocalDate.now(), PostSignScreen.MOVE, mockData.quoteBundle.displayName))
    }

    override fun onOpenCheckout() {
        _events.tryEmit(
            Event.OpenCheckout(
                CheckoutParameter(
                    quoteIds = listOf(mockData.quoteBundle.quotes[0].id)
                )
            )
        )
    }

    override fun reload() {
        shouldError = false
        load()
    }

    override fun onDiscardOffer() {
        _events.tryEmit(Event.DiscardOffer)
    }

    override fun onGoToDirectDebit() {
    }

    override fun onSwedishBankIdSign() {
    }

    private fun load() {
        viewModelScope.launch {
            delay(650)
            if (shouldError) {
                _events.tryEmit(Event.Error())
                return@launch
            }
            val topOfferItems = OfferItemsBuilder.createTopOfferItems(mockData)
            val perilItems = OfferItemsBuilder.createPerilItems(mockData.quoteBundle.quotes)
            val documentItems = OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes)
            val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes)
            val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems(mockData)
            _viewState.value =
                ViewState(
                    topOfferItems = topOfferItems,
                    perils = perilItems,
                    documents = documentItems,
                    insurableLimitsItems = insurableLimitsItems,
                    bottomOfferItems = bottomOfferItems,
                    signMethod = mockData.signMethodForQuotes,
                    checkoutTextRes = mockData.checkoutTextRes(),
                    title = mockData.quoteBundle.appConfiguration.title,
                    loginStatus = LoginStatus.LOGGED_IN
                )
        }
    }

    companion object {
        var shouldError = false
        var mockData = OFFER_DATA_SWEDISH_APARTMENT
    }
}
