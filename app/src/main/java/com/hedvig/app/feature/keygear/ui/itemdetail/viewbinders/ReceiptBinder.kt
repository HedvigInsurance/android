package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.ReceiptActivity
import com.hedvig.app.feature.keygear.ui.itemdetail.ReceiptFileUploadBottomSheet
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.key_gear_item_detail_receipt_section.view.*

class ReceiptBinder(
    private val root: LinearLayout,
    private val supportFragmentManager: FragmentManager,
    private val tracker: KeyGearTracker
) {
    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        data.fragments.keyGearItemFragment.receipts.getOrNull(0)?.let { receipt ->
            root.addOrViewReceipt.text = root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_SHOW)
            root.addOrViewReceipt.setHapticClickListener {
                tracker.showReceipt()
                root.context.startActivity(
                    ReceiptActivity.newInstance(
                        root.context,
                        receipt.file.preSignedUrl
                    )
                )
            }
        } ?: run {
            root.addOrViewReceipt.text =
                root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_RECEIPT_CELL_ADD_BUTTON)
            root.addOrViewReceipt.setHapticClickListener {
                tracker.addReceipt()
                ReceiptFileUploadBottomSheet
                    .newInstance()
                    .show(supportFragmentManager, ReceiptFileUploadBottomSheet.TAG)
            }
        }
    }
}
