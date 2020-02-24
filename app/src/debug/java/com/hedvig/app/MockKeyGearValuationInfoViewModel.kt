package com.hedvig.app

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.KeyGearValuationInfoViewModel

class MockKeyGearValuationInfoViewModel : KeyGearValuationInfoViewModel() {
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

    companion object {
        val item = KeyGearItemFragment(
            "KeyGearItem",
            "123",
            "Sak",
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
            null,
            null,
            KeyGearItemFragment.Deductible(
                "MonetaryAmountV2",
                "1500.00"
            ),
            KeyGearItemFragment.AsKeyGearItemValuationFixed(
                "KeyGearItemValuationFixed",
                90,
                KeyGearItemFragment.Valuation1(
                    "MonetaryAmountV2",
                    "9000.00"
                )
            ),
            listOf(),
            listOf()
        )
    }
}
