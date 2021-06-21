package com.hedvig.app.feature.offer

import android.os.Handler
import android.os.Looper.getMainLooper
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import java.time.LocalDate

class MockOfferViewModel : OfferViewModel() {

    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        Handler(getMainLooper()).postDelayed(
            {
                val offerItems = OfferItemsBuilder.createOfferItems(mockData)
                val documentItems = OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes[0])
                val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes[0])
                _viewState.postValue(ViewState.OfferItems(offerItems, documentItems, insurableLimitsItems))
            },
            500
        )
    }

    override fun removeDiscount() = Unit
    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = Unit
    override fun triggerOpenChat(done: () -> Unit) = Unit
    override fun startSign() = Unit
    override fun clearPreviousErrors() = Unit
    override fun manuallyRecheckSignStatus() = Unit
    override fun chooseStartDate(id: String, date: LocalDate) {
        _viewState.postValue(
            ViewState.OfferItems(
                OfferItemsBuilder.createOfferItems(
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
                listOf(),
                listOf()
            )
        )
    }

    override fun removeStartDate(id: String) {
        _viewState.postValue(
            ViewState.OfferItems(
                OfferItemsBuilder.createOfferItems(
                    mockData.copy(
                        quoteBundle = mockData.quoteBundle.copy(
                            quotes = mockData.quoteBundle.quotes.map {
                                it.copy(startDate = null)
                            }
                        )
                    )
                ),
                listOf(),
                listOf(),
            )
        )
    }

    companion object {
        var mockData = OFFER_DATA_SWEDISH_APARTMENT
    }
}
