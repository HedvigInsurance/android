package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.fragment.KeyGearItemValuationFragment
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
                        fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
                            KeyGearItemFragment(
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
                                        valuation = KeyGearItemValuationFragment.Valuation1(
                                            asKeyGearItemValuationFixed = KeyGearItemValuationFragment.AsKeyGearItemValuationFixed(
                                                ratio = 31,
                                                valuation = KeyGearItemValuationFragment.Valuation(
                                                    amount = "1234.00"
                                                )
                                            ),
                                            asKeyGearItemValuationMarketValue = null
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    KeyGearItemsQuery.KeyGearItem(
                        fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
                            KeyGearItemFragment(
                                id = "234",
                                name = "Mackapär",
                                physicalReferenceHash = null,
                                photos = listOf(
                                    KeyGearItemFragment.Photo(
                                        file = KeyGearItemFragment.File(
                                            preSignedUrl = "https://images.unsplash.com/photo-1522199755839-a2bacb67c546"
                                        )
                                    )
                                ),
                                receipts = emptyList(),
                                category = KeyGearItemCategory.COMPUTER,
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
                                        valuation = KeyGearItemValuationFragment.Valuation1(
                                            asKeyGearItemValuationFixed = KeyGearItemValuationFragment.AsKeyGearItemValuationFixed(
                                                ratio = 31,
                                                valuation = KeyGearItemValuationFragment.Valuation(
                                                    amount = "1234.00"
                                                )
                                            ),
                                            asKeyGearItemValuationMarketValue = null
                                        )
                                    )
                                )
                            )
                        )
                    ),

                    KeyGearItemsQuery.KeyGearItem(
                        fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
                            KeyGearItemFragment(
                                id = "345",
                                name = null,
                                physicalReferenceHash = null,
                                photos = emptyList(),
                                receipts = emptyList(),
                                category = KeyGearItemCategory.JEWELRY,
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
                                        valuation = KeyGearItemValuationFragment.Valuation1(
                                            asKeyGearItemValuationFixed = KeyGearItemValuationFragment.AsKeyGearItemValuationFixed(
                                                ratio = 31,
                                                valuation = KeyGearItemValuationFragment.Valuation(
                                                    amount = "1234.00"
                                                )
                                            ),
                                            asKeyGearItemValuationMarketValue = null
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
