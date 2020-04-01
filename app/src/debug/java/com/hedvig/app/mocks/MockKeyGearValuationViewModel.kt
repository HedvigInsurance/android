package com.hedvig.app.mocks

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.fragment.KeyGearItemValuationFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import org.threeten.bp.LocalDate

class MockKeyGearValuationViewModel : KeyGearValuationViewModel() {
    override val uploadResult = MutableLiveData<KeyGearItemQuery.Data>()
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override fun loadItem(id: String) {
        Handler().postDelayed({
            data.postValue(
                KeyGearItemQuery.KeyGearItem(
                    fragments = KeyGearItemQuery.KeyGearItem.Fragments(item)
                )
            )
        }, 250)
    }

    override fun updatePurchaseDateAndPrice(
        id: String,
        date: LocalDate,
        price: MonetaryAmountV2Input
    ) {
        data.postValue(
            KeyGearItemQuery.KeyGearItem(
                fragments = KeyGearItemQuery.KeyGearItem.Fragments(
                    item.copy(
                        purchasePrice = KeyGearItemFragment.PurchasePrice(
                            amount = price.amount
                        )
                    )
                )
            )
        )
        Handler().postDelayed({
            // uploadResult.postValue()
        }, 500L)
    }

    companion object {
        val item = KeyGearItemFragment(
            id = "123",
            name = "Sak",
            physicalReferenceHash = null,
            photos = listOf(
                KeyGearItemFragment.Photo(
                    file = KeyGearItemFragment.File(
                        preSignedUrl = "https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"
                    )
                )
            ),
            receipts = emptyList(),
            category = KeyGearItemCategory.PHONE,
            purchasePrice = null,
            timeOfPurchase = null,
            deductible = KeyGearItemFragment.Deductible(
                amount = "1500.00"
            ),
            covered = emptyList(),
            maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
                amount = "50000"
            ),
            exceptions = emptyList(),
            deleted = false,
            fragments = KeyGearItemFragment.Fragments(
                KeyGearItemValuationFragment(
                    valuation = null
                )
            )
        )
    }
}
