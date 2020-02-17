package com.hedvig.app

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import org.threeten.bp.YearMonth

class MockKeyGearValuationViewModel : KeyGearValuationViewModel() {
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override fun updatePurchaseDateAndPrice(
        id: String,
        yearMonth: YearMonth,
        price: MonetaryAmountV2Input
    ) {
        val id = data.value?.fragments?.keyGearItemFragment?.id ?: return
        Handler().postDelayed({
            data.postValue(
                KeyGearItemQuery.KeyGearItem(
                    "KeyGearItem",
                    KeyGearItemQuery.KeyGearItem.Fragments(
                        MockKeyGearItemDetailViewModel.items[id]!!.toBuilder().timeOfPurchase(
                            YearMonth.now()
                        )
                            .purchasePrice(
                                KeyGearItemFragment.PurchasePrice(
                                    "MonetaryAmountV2",
                                    "123"
                                )
                            )
                            .build()
                    )
                )
            )
        }, 2000)
    }
}
