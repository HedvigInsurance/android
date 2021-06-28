package com.hedvig.app.feature.offer

import android.os.Handler
import android.os.Looper.getMainLooper
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.app.feature.offer.quotedetail.buildDocuments
import com.hedvig.app.feature.offer.quotedetail.buildInsurableLimits
import com.hedvig.app.feature.offer.quotedetail.buildPerils
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import java.time.LocalDate

class MockOfferViewModel : OfferViewModel() {

    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        Handler(getMainLooper()).postDelayed(
            {
                val topOfferItems = OfferItemsBuilder.createTopOfferItems(mockData)
                val perilItems = OfferItemsBuilder.createPerilItems(mockData.quoteBundle.quotes)
                val documentItems = OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes)
                val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes)
                val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems()
                _viewState.postValue(
                    ViewState.OfferItems(
                        topOfferItems,
                        perilItems,
                        documentItems,
                        insurableLimitsItems,
                        bottomOfferItems
                    )
                )
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
                OfferItemsBuilder.createPerilItems(mockData.quoteBundle.quotes),
                OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes),
                OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes),
                OfferItemsBuilder.createBottomOfferItems(),
            )
        )
    }

    override fun removeStartDate(id: String) {
        _viewState.postValue(
            ViewState.OfferItems(
                OfferItemsBuilder.createTopOfferItems(
                    mockData.copy(
                        quoteBundle = mockData.quoteBundle.copy(
                            quotes = mockData.quoteBundle.quotes.map {
                                it.copy(startDate = null)
                            }
                        )
                    )
                ),
                OfferItemsBuilder.createPerilItems(mockData.quoteBundle.quotes),
                OfferItemsBuilder.createDocumentItems(mockData.quoteBundle.quotes),
                OfferItemsBuilder.createInsurableLimits(mockData.quoteBundle.quotes),
                OfferItemsBuilder.createBottomOfferItems(),
            )
        )
    }

    override suspend fun getQuoteDetailItems(
        id: String,
    ): QuoteDetailItems {
        val quote = mockData.quoteBundle.quotes.first { it.id == id }
        return QuoteDetailItems(
            quote.displayName,
            buildPerils(quote),
            buildInsurableLimits(quote),
            buildDocuments(quote)
        )
    }

    companion object {
        var mockData = OFFER_DATA_SWEDISH_APARTMENT
    }
}
