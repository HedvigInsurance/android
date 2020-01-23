package com.hedvig.app

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.android.owldroid.type.InsuranceType
import com.hedvig.app.feature.offer.OfferViewModel
import org.threeten.bp.LocalDate

class MockOfferViewModel(
    private val context: Context
) : OfferViewModel() {
    override val data = MutableLiveData<OfferQuery.Data>()
    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        val activePersona = context
            .getSharedPreferences(DevelopmentActivity.DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)

        data.postValue(
            when (activePersona) {
                0 -> UNSIGNED_WITH_APARTMENT
                1 -> UNSIGNED_WITH_HOUSE
                else -> UNSIGNED_WITH_APARTMENT
            }
        )
    }

    override fun removeDiscount() = Unit
    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = Unit
    override fun triggerOpenChat(done: () -> Unit) = Unit
    override fun startSign() = Unit
    override fun clearPreviousErrors() = Unit
    override fun manuallyRecheckSignStatus() = Unit
    override fun chooseStartDate(id: String, date: LocalDate) = Unit

    companion object {
        private val UNSIGNED_WITH_APARTMENT = OfferQuery.Data(
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
                42,
                InsuranceType.BRF,
                "http://www.africau.edu/images/default/sample.pdf",
                "http://www.africau.edu/images/default/sample.pdf",
                null,
                null,
                null,
                null,
                null,
                OfferQuery.ArrangedPerilCategories(
                    "ArrangedPerilCategories",
                    null,
                    null,
                    null
                ),
                null
            ),
            listOf(),
            OfferQuery.AsCompleteQuote(
                "CompleteQuote",
                LocalDate.of(2020, 2, 1),
                "ea656f5f-40b2-4953-85d9-752b33e69e38"
            )
        )

        private val UNSIGNED_WITH_HOUSE = OfferQuery.Data(
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
                42,
                InsuranceType.HOUSE,
                "http://www.africau.edu/images/default/sample.pdf",
                "http://www.africau.edu/images/default/sample.pdf",
                30,
                1992,
                2,
                listOf(),
                true,
                OfferQuery.ArrangedPerilCategories(
                    "ArrangedPerilCategories",
                    null,
                    null,
                    null
                ),
                null
            ),
            listOf(),
            OfferQuery.AsCompleteQuote(
                "CompleteQuote",
                LocalDate.of(2020, 2, 1),
                "ea656f5f-40b2-4953-85d9-752b33e69e38"
            )
        )
    }
}
