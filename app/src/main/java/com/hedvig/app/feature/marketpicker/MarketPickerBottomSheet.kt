package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.ListBottomSheetBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MarketPickerBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(ListBottomSheetBinding::bind)
    private val tracker: MarketPickerTracker by inject()
    private val viewModel: MarketPickerViewModel by sharedViewModel()
    private val marketManager: MarketManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.list_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            recycler.adapter = MarketPickerBottomSheetAdapter(viewModel, tracker, dialog).also {
                it.submitList(
                    listOf(
                        MarketAdapterModel.Header,
                        MarketAdapterModel.MarketList(marketManager.enabledMarkets)
                    )
                )
            }
        }
    }

    companion object {
        const val TAG = "MarketPickerBottomSheet"
    }
}
