package com.hedvig.app

import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import org.threeten.bp.YearMonth

class MockKeyGearValuationViewModel : KeyGearValuationViewModel() {

    override fun updatePurchaseDateAndPrice(
        id: String,
        yearMonth: YearMonth,
        price: MonetaryAmountV2Input
    ) {
    }
}
