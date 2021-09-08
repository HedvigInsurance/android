package com.hedvig.app.mocks

import android.net.Uri
import android.os.Handler
import android.os.Looper.getMainLooper
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.fragment.KeyGearItemValuationFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.util.LiveEvent

class MockKeyGearItemDetailViewModel : KeyGearItemDetailViewModel() {
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override val isUploading = LiveEvent<Boolean>()
    override val isDeleted = LiveEvent<Boolean>()

    override fun updateItemName(newName: String) {
        val id = data.value?.fragments?.keyGearItemFragment?.id ?: return
        Handler(getMainLooper()).postDelayed(
            {
                data.postValue(
                    KeyGearItemQuery.KeyGearItem(
                        "KeyGearItem",
                        KeyGearItemQuery.KeyGearItem.Fragments(
                            items[id]!!.copy(name = newName)
                        )
                    )
                )
            },
            250
        )
    }

    override fun deleteItem() {
        Handler(getMainLooper()).postDelayed(
            {
                isDeleted.postValue(true)
            },
            250
        )
    }

    override fun loadItem(id: String) {
        Handler(getMainLooper()).postDelayed(
            {
                data.postValue(
                    KeyGearItemQuery.KeyGearItem(
                        "KeyGearItem",
                        KeyGearItemQuery.KeyGearItem.Fragments(items[id]!!)
                    )
                )
            },
            250
        )
    }

    override fun uploadReceipt(uri: Uri) {
        val id = data.value?.fragments?.keyGearItemFragment?.id ?: return
        isUploading.value = true
        Handler(getMainLooper()).postDelayed(
            {
                data.postValue(
                    KeyGearItemQuery.KeyGearItem(
                        fragments = KeyGearItemQuery.KeyGearItem.Fragments(
                            items[id]!!.copy(
                                receipts = listOf(
                                    KeyGearItemFragment.Receipt(
                                        file = KeyGearItemFragment.File1(
                                            preSignedUrl =
                                            "https://upload.wikimedia.org/wikipedia/commons/0/0b/ReceiptSwiss.jpg"
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
                isUploading.postValue(false)
            },
            2000
        )
    }

    companion object {
        val items = hashMapOf(
            "123" to
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
                    receipts = listOf(),
                    category = KeyGearItemCategory.PHONE,
                    purchasePrice = KeyGearItemFragment.PurchasePrice(
                        amount = "60000"
                    ),
                    timeOfPurchase = null,
                    deductible = KeyGearItemFragment.Deductible(
                        amount = "1500"
                    ),
                    maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
                        amount = "50000"
                    ),
                    deleted = false,
                    fragments = KeyGearItemFragment.Fragments(
                        KeyGearItemValuationFragment(
                            "KeyGearItemValuationFixed",
                            KeyGearItemValuationFragment.Valuation1(
                                asKeyGearItemValuationFixed = KeyGearItemValuationFragment.AsKeyGearItemValuationFixed(
                                    ratio = 90,
                                    valuation = KeyGearItemValuationFragment.Valuation(
                                        amount = "9000.00"
                                    )
                                ),
                                asKeyGearItemValuationMarketValue = null
                            )
                        )
                    )
                ),
            "234" to
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
                    receipts = listOf(),
                    category = KeyGearItemCategory.COMPUTER,
                    purchasePrice = KeyGearItemFragment.PurchasePrice(
                        amount = "20000"
                    ),
                    timeOfPurchase = null,
                    deductible = KeyGearItemFragment.Deductible(
                        amount = "1500"
                    ),
                    maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
                        amount = "50000"
                    ),
                    deleted = false,
                    fragments = KeyGearItemFragment.Fragments(
                        KeyGearItemValuationFragment(
                            valuation = KeyGearItemValuationFragment.Valuation1(
                                asKeyGearItemValuationFixed = KeyGearItemValuationFragment.AsKeyGearItemValuationFixed(
                                    ratio = 31,
                                    valuation = KeyGearItemValuationFragment.Valuation(
                                        amount = "55000.00"
                                    )
                                ),
                                asKeyGearItemValuationMarketValue = null
                            )
                        )
                    )
                ),
            "345" to
                KeyGearItemFragment(
                    id = "345",
                    name = null,
                    physicalReferenceHash = null,
                    photos = emptyList(),
                    receipts = listOf(
                        KeyGearItemFragment.Receipt(
                            file = KeyGearItemFragment.File1(
                                preSignedUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
                            )
                        )
                    ),
                    category = KeyGearItemCategory.JEWELRY,
                    purchasePrice = null,
                    timeOfPurchase = null,
                    deductible = KeyGearItemFragment.Deductible(
                        amount = "1500"
                    ),
                    maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
                        amount = "50000"
                    ),
                    deleted = false,
                    fragments = KeyGearItemFragment.Fragments(
                        KeyGearItemValuationFragment(
                            valuation = null
                        )
                    )
                )
        )
    }
}
