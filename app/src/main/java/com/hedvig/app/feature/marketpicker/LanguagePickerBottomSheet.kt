package com.hedvig.app.feature.marketpicker

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.LanguagePickerBottomSheetBinding
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.extensions.viewBinding

class LanguagePickerBottomSheet(
    private val viewModel: MarketPickerViewModel
) : BottomSheetDialogFragment() {
    val binding by viewBinding(LanguagePickerBottomSheetBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.language_picker_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            recycler.adapter = LanguagePickerBottomSheetAdapter(viewModel)
            viewModel.data.observe(viewLifecycleOwner) { VMState ->
                VMState.market?.let { market ->
                    (recycler.adapter as LanguagePickerBottomSheetAdapter).items =
                        Language.getAvailableLanguages(market)
                }
                (recycler.adapter as LanguagePickerBottomSheetAdapter).notifyDataSetChanged()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.save()
    }

    companion object {
        const val TAG = "LanguagePickerBottomSheet"
    }
}
