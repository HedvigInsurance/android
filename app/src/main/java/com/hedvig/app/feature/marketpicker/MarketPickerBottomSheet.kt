package com.hedvig.app.feature.marketpicker

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.MarketPickerBottomSheetBinding
import com.hedvig.app.util.extensions.viewBinding

class MarketPickerBottomSheet(
    private val markets: List<Market>,
    private val viewModel: MarketPickerViewModel,
    private val tracker: MarketPickerTracker
) : BottomSheetDialogFragment() {
    val binding by viewBinding(MarketPickerBottomSheetBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.market_picker_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            header.text = header.context.getString(R.string.market_picker_modal_title)
            recycler.adapter = MarketPickerBottomSheetAdapter(viewModel, tracker, dialog).also {
                it.items = markets
            }
            viewModel.data.observe(viewLifecycleOwner) {
                (recycler.adapter as MarketPickerBottomSheetAdapter).notifyDataSetChanged()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.save()
    }

    companion object {
        const val TAG = "MarketPickerBottomSheet"
    }
}
