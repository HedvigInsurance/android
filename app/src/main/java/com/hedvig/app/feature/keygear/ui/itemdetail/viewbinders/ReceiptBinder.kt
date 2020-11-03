package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.KeyGearItemDetailReceiptSectionBinding
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.ReceiptActivity
import com.hedvig.app.feature.keygear.ui.itemdetail.ReceiptFileUploadBottomSheet
import com.hedvig.app.util.extensions.view.setHapticClickListener

class ReceiptBinder(
    private val binding: KeyGearItemDetailReceiptSectionBinding,
    private val supportFragmentManager: FragmentManager,
    private val tracker: KeyGearTracker
) {
    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        data.fragments.keyGearItemFragment.receipts.getOrNull(0)?.let { receipt ->
            binding.addOrViewReceipt.setText(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_SHOW)
            binding.addOrViewReceipt.setHapticClickListener {
                tracker.showReceipt()
                binding.root.context.startActivity(
                    ReceiptActivity.newInstance(
                        binding.root.context,
                        receipt.file.preSignedUrl
                    )
                )
            }
        } ?: run {
            binding.addOrViewReceipt.setText(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_ADD_BUTTON)
            binding.addOrViewReceipt.setHapticClickListener {
                tracker.addReceipt()
                ReceiptFileUploadBottomSheet
                    .newInstance()
                    .show(supportFragmentManager, ReceiptFileUploadBottomSheet.TAG)
            }
        }
    }
}
