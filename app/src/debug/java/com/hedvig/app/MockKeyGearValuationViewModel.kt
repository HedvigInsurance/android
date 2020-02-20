package com.hedvig.app

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import org.threeten.bp.LocalDate

class MockKeyGearValuationViewModel : KeyGearValuationViewModel() {
    override val finishedUploading = MutableLiveData<Boolean>()
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override fun loadItem(id: String) {
        Handler().postDelayed({
            data.postValue(
                KeyGearItemQuery.KeyGearItem(
                    "KeyGearItem",
                    KeyGearItemQuery.KeyGearItem.Fragments(item)
                )
            )
        }, 250)
    }

    override fun updatePurchaseDateAndPrice(
        id: String,
        date: LocalDate,
        price: MonetaryAmountV2Input
    ) {
        Handler().postDelayed({
            finishedUploading.postValue(true)
        }, 500L)
    }

    companion object {
        val item = KeyGearItemFragment(
            "KeyGearItem",
            "123",
            listOf(
                KeyGearItemFragment.Photo(
                    "KeyGearItemPhoto",
                    KeyGearItemFragment.File(
                        "S3File",
                        "https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"
                    )
                )
            ),
            listOf(),
            KeyGearItemCategory.PHONE,
            KeyGearItemFragment.PurchasePrice("MonetaryAmountV2", "1234"),
            null,
            KeyGearItemFragment.AsKeyGearItemValuationFixed(
                "KeyGearItemValuation",
                31,
                KeyGearItemFragment.Valuation1(
                    "KeyGearItemValuationFixed",
                    "1234"
                )
            )
        )
    }
}
