package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.ListBottomSheetBinding
import com.hedvig.app.feature.settings.Language
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LanguagePickerBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(ListBottomSheetBinding::bind)

    private val model: MarketPickerViewModel by sharedViewModel()
    private val tracker: MarketPickerTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.list_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            recycler.adapter = LanguagePickerBottomSheetAdapter(model, tracker, dialog)
            model.pickerState.observe(viewLifecycleOwner) { state ->
                state.market.let { market ->
                    (recycler.adapter as? LanguagePickerBottomSheetAdapter)?.submitList(
                        listOf(
                            LanguageAdapterModel.Header,
                            LanguageAdapterModel.Description,
                        ) +
                            Language.getAvailableLanguages(market).map { l -> LanguageAdapterModel.LanguageItem(l) }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "LanguagePickerBottomSheet"
    }
}
