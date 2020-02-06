package com.hedvig.app.feature.keygear.ui.itemdetail

import android.os.Bundle
import com.hedvig.app.ui.fragment.YearMonthPickerDialog
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.threeten.bp.YearMonth

class PurchaseDateYearMonthPicker : YearMonthPickerDialog() {
    private val model: KeyGearItemDetailViewModel by sharedViewModel()

    override val title: String
        get() = requireArguments().getString(TITLE, "")

    override fun onSubmit(yearMonth: YearMonth) {
        model.choosePurchaseDate(yearMonth)
        dismiss()
    }

    companion object {
        const val TAG = "YearMonthPickerDialog"

        private const val TITLE = "TITLE"

        fun newInstance(title: String) = PurchaseDateYearMonthPicker().apply {
            arguments = Bundle().also { b ->
                b.putString(TITLE, title)
            }
        }
    }
}
