package com.hedvig.app.feature.offer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MockOfferViewModel : OfferViewModel() {

    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading(OfferItemsBuilder.createLoadingItem())
            delay(150)
            val topOfferItems = OfferItemsBuilder.createTopOfferItems(mockData)
            val perilItems = OfferItemsBuilder.createPerilItems(mockData.quoteBundle.quotes[0])
            val documentItems = OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes[0])
            val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes[0])
            val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems()
            _viewState.value =
                ViewState.OfferItems(
                    topOfferItems,
                    perilItems,
                    documentItems,
                    insurableLimitsItems,
                    bottomOfferItems
                )
        }
    }

    override fun removeDiscount() = Unit
    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = Unit
    override fun triggerOpenChat(done: () -> Unit) = Unit
    override fun startSign() = Unit
    override fun clearPreviousErrors() = Unit
    override fun manuallyRecheckSignStatus() = Unit
    override fun chooseStartDate(id: String, date: LocalDate) {
        _viewState.value = ViewState.OfferItems(
            OfferItemsBuilder.createTopOfferItems(
                mockData.copy(
                    quoteBundle = mockData.quoteBundle.copy(
                        quotes = mockData.quoteBundle.quotes.map {
                            it.copy(
                                startDate = date
                            )
                        }
                    )
                )
            ),
            OfferItemsBuilder.createPerilItems(mockData.quoteBundle.quotes[0]),
            OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes[0]),
            OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes[0]),
            OfferItemsBuilder.createBottomOfferItems(),
        )
    }

    override fun removeStartDate(id: String) {
        _viewState.value = ViewState.OfferItems(
            OfferItemsBuilder.createTopOfferItems(
                mockData.copy(
                    quoteBundle = mockData.quoteBundle.copy(
                        quotes = mockData.quoteBundle.quotes.map {
                            it.copy(startDate = null)
                        }
                    )
                )
            ),
            OfferItemsBuilder.createPerilItems(mockData.quoteBundle.quotes[0]),
            OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes[0]),
            OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes[0]),
            OfferItemsBuilder.createBottomOfferItems(),
        )
    }

    companion object {
        var mockData = OFFER_DATA_SWEDISH_APARTMENT
    }
}
