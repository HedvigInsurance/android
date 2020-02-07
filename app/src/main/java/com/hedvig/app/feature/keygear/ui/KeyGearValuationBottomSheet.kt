package com.hedvig.app.feature.keygear.ui

import android.app.Dialog
import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.PurchaseDateYearMonthPicker
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.observe
import kotlinx.android.synthetic.main.dialog_key_gear_valuation.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.DateFormatSymbols

class KeyGearValuationBottomSheet : RoundedBottomSheetDialogFragment() {

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    private val model: KeyGearItemDetailViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.dialog_key_gear_valuation)

        dialog.dateInput.setOnClickListener {
            fragmentManager?.let { fragmentManager ->
                PurchaseDateYearMonthPicker.newInstance("title")
                    .show(fragmentManager, PurchaseDateYearMonthPicker.TAG)
            }
        }

        model.purchaseDate.observe(this) { yearMonth ->
            yearMonth?.let {
                dialog.dateInput.text =
                    "${DateFormatSymbols().months[yearMonth.month.value - 1]} ${yearMonth.year}"
            }
        }

        return dialog
    }

    companion object {
        const val TAG = "keyGearValuationBottomSheet"

        fun newInstance() = KeyGearValuationBottomSheet()
    }
}
