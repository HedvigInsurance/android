package com.hedvig.app.feature.offer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.app.feature.offer.quotedetail.buildDocuments
import com.hedvig.app.feature.offer.quotedetail.buildInsurableLimits
import com.hedvig.app.feature.offer.quotedetail.buildPerils
import com.hedvig.app.feature.offer.ui.checkout.CheckoutParameter
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

class MockOfferViewModel : OfferViewModel() {

    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        viewModelScope.launch {
            delay(150)
            val topOfferItems = OfferItemsBuilder.createTopOfferItems(mockData)
            val perilItems = OfferItemsBuilder.createPerilItems(mockData.quoteBundle.quotes)
            val documentItems = OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes)
            val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes)
            val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems(mockData.quoteBundle)
            _viewState.value =
                ViewState.Offer(
                    topOfferItems,
                    perilItems,
                    documentItems,
                    insurableLimitsItems,
                    bottomOfferItems,
                    mockData.signMethodForQuotes
                )
        }
    }

    override fun removeDiscount() = Unit
    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = Unit
    override fun triggerOpenChat(done: () -> Unit) = Unit
    override fun startSign() = Unit
    override fun clearPreviousErrors() = Unit
    override fun manuallyRecheckSignStatus() = Unit

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
        _events.tryEmit(Event.ApproveSuccessful(LocalDate.now()))
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

    companion object {
        var mockData = OFFER_DATA_SWEDISH_APARTMENT
    }
}
