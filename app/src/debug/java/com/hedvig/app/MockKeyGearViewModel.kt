package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel

class MockKeyGearViewModel : KeyGearViewModel() {
    override val data = MutableLiveData<KeyGearItemsQuery.Data>()
    override fun sendAutoAddedItems() {}

    init {
        data.postValue(
            KeyGearItemsQuery.Data(
                listOf(
                    KeyGearItemsQuery.KeyGearItem(
                        "KeyGearItem",
                        KeyGearItemsQuery.KeyGearItem.Fragments(
                            KeyGearItemFragment(
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
                                    "KeyGearItemValuation",
                                    31,
                                    KeyGearItemFragment.Valuation1(
                                        "KeyGearItemValuationFixed",
                                        "1234"
                                    )
                                ),
                                listOf(),
                                listOf()
                            )
                        )
                    ),
                    KeyGearItemsQuery.KeyGearItem(
                        "KeyGearItem",
                        KeyGearItemsQuery.KeyGearItem.Fragments(
                            KeyGearItemFragment(
                                "KeyGearItem",
                                "234",
                                "Mackapär",
                                listOf(
                                    KeyGearItemFragment.Photo(
                                        "KeyGearItemPhoto",
                                        KeyGearItemFragment.File(
                                            "S3File",
                                            "https://images.unsplash.com/photo-1522199755839-a2bacb67c546"
                                        )
                                    )
                                ),
                                listOf(),
                                KeyGearItemCategory.COMPUTER,
                                null,
                                null,
                                KeyGearItemFragment.Deductible(
                                    "MonetaryAmountV2",
                                    "1500.00"
                                ),
                                KeyGearItemFragment.AsKeyGearItemValuationFixed(
                                    "KeyGearItemValuation",
                                    31,
                                    KeyGearItemFragment.Valuation1(
                                        "KeyGearItemValuationFixed",
                                        "1234"
                                    )
                                ),
                                listOf(),
                                listOf()
                            )
                        )
                    ),

                    KeyGearItemsQuery.KeyGearItem(
                        "KeyGearItem",
                        KeyGearItemsQuery.KeyGearItem.Fragments(
                            KeyGearItemFragment(
                                "KeyGearItem",
                                "345",
                                null,
                                listOf(),
                                listOf(),
                                KeyGearItemCategory.JEWELRY,
                                null,
                                null,
                                KeyGearItemFragment.Deductible(
                                    "MonetaryAmountV2",
                                    "1500.00"
                                ),
                                KeyGearItemFragment.AsKeyGearItemValuationFixed(
                                    "KeyGearItemValuation",
                                    31,
                                    KeyGearItemFragment.Valuation1(
                                        "KeyGearItemValuationFixed",
                                        "1234"
                                    )
                                ),
                                listOf(),
                                listOf()
                            )
                        )
                    )
                )
            )
        )
    }
}
