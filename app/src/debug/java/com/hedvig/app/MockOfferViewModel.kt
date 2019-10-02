package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.app.feature.offer.OfferViewModel
import fragment.SignStatusFragment
import type.InsuranceStatus
import type.InsuranceType

class MockOfferViewModel : OfferViewModel() {
    override val data = MutableLiveData<OfferQuery.Data>()
    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        data.postValue(
            OfferQuery.Data(
                OfferQuery.Insurance(
                    "Insurance",
                    InsuranceStatus.PENDING,
                    "Testvägen 1",
                    2,
                    OfferQuery.PreviousInsurer(
                        "PreviousInsurer",
                        "Folksam",
                        true
                    ),
                    InsuranceType.BRF,
                    "http://www.africau.edu/images/default/sample.pdf",
                    "http://www.africau.edu/images/default/sample.pdf",
                    OfferQuery.ArrangedPerilCategory(
                        "ArrangedPerilCategories",
                        null,
                        null,
                        null
                    ),
                    null
                ),
                listOf()
            )
        )
    }

    override fun removeDiscount() = Unit
    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = Unit
    override fun triggerOpenChat(done: () -> Unit) = Unit
    override fun startSign() = Unit
    override fun clearPreviousErrors() = Unit
    override fun manuallyRecheckSignStatus() = Unit
}
