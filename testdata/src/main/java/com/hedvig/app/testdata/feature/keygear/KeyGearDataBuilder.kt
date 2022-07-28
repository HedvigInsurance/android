package com.hedvig.app.testdata.feature.keygear

import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.graphql.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.fragment.KeyGearItemValuationFragment
import com.hedvig.android.owldroid.graphql.type.KeyGearItemCategory
import com.hedvig.android.owldroid.graphql.type.KeyGearItemValuationFixed

class KeyGearDataBuilder {

  fun build() = KeyGearItemsQuery.Data(
    listOf(
      KeyGearItemsQuery.KeyGearItem(
        __typename = "",
        fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
          KeyGearItemFragment(
            __typename = "",
            id = "123",
            name = "Sak",
            physicalReferenceHash = null,
            photos = listOf(
              KeyGearItemFragment.Photo(
                file = KeyGearItemFragment.File(
                  preSignedUrl = "https://images.unsplash.com/photo-1505156868547-9b49f4df4e04",
                ),
              ),
            ),
            receipts = emptyList(),
            category = KeyGearItemCategory.PHONE,
            purchasePrice = null,
            timeOfPurchase = null,
            deductible = KeyGearItemFragment.Deductible(
              amount = "1500.00",
            ),
            maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
              amount = "50000",
            ),
            deleted = false,
            fragments = KeyGearItemFragment.Fragments(
              KeyGearItemValuationFragment(
                valuation = KeyGearItemValuationFragment.Valuation1(
                  __typename = KeyGearItemValuationFixed.type.name,
                  asKeyGearItemValuationFixed = KeyGearItemValuationFragment
                    .AsKeyGearItemValuationFixed(
                      __typename = KeyGearItemValuationFixed.type.name,
                      ratio = 31,
                      valuation = KeyGearItemValuationFragment.Valuation(
                        amount = "1234.00",
                      ),
                    ),
                  asKeyGearItemValuationMarketValue = null,
                ),
              ),
            ),
          ),
        ),
      ),
      KeyGearItemsQuery.KeyGearItem(
        __typename = "",
        fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
          KeyGearItemFragment(
            __typename = "",
            id = "234",
            name = "Mackapär",
            physicalReferenceHash = null,
            photos = listOf(
              KeyGearItemFragment.Photo(
                file = KeyGearItemFragment.File(
                  preSignedUrl = "https://images.unsplash.com/photo-1522199755839-a2bacb67c546",
                ),
              ),
            ),
            receipts = emptyList(),
            category = KeyGearItemCategory.COMPUTER,
            purchasePrice = null,
            timeOfPurchase = null,
            deductible = KeyGearItemFragment.Deductible(
              amount = "1500.00",
            ),
            maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
              amount = "50000",
            ),
            deleted = false,
            fragments = KeyGearItemFragment.Fragments(
              KeyGearItemValuationFragment(
                valuation = KeyGearItemValuationFragment.Valuation1(
                  __typename = KeyGearItemValuationFixed.type.name,
                  asKeyGearItemValuationFixed = KeyGearItemValuationFragment
                    .AsKeyGearItemValuationFixed(
                      __typename = KeyGearItemValuationFixed.type.name,
                      ratio = 31,
                      valuation = KeyGearItemValuationFragment.Valuation(
                        amount = "1234.00",
                      ),
                    ),
                  asKeyGearItemValuationMarketValue = null,
                ),
              ),
            ),
          ),
        ),
      ),

      KeyGearItemsQuery.KeyGearItem(
        __typename = "",
        fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
          KeyGearItemFragment(
            __typename = "",
            id = "345",
            name = null,
            physicalReferenceHash = null,
            photos = emptyList(),
            receipts = emptyList(),
            category = KeyGearItemCategory.JEWELRY,
            purchasePrice = null,
            timeOfPurchase = null,
            deductible = KeyGearItemFragment.Deductible(
              amount = "1500.00",
            ),
            maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
              amount = "50000",
            ),
            deleted = false,
            fragments = KeyGearItemFragment.Fragments(
              KeyGearItemValuationFragment(
                valuation = KeyGearItemValuationFragment.Valuation1(
                  __typename = KeyGearItemValuationFixed.type.name,
                  asKeyGearItemValuationFixed = KeyGearItemValuationFragment
                    .AsKeyGearItemValuationFixed(
                      __typename = KeyGearItemValuationFixed.type.name,
                      ratio = 31,
                      valuation = KeyGearItemValuationFragment.Valuation(
                        amount = "1234.00",
                      ),
                    ),
                  asKeyGearItemValuationMarketValue = null,
                ),
              ),
            ),
          ),
        ),
      ),
    ),
  )
}
