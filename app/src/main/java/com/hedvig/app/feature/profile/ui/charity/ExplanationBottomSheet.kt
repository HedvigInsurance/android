package com.hedvig.app.feature.profile.ui.charity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.BottomSheetExplanationBinding
import com.hedvig.app.util.extensions.setMarkdownText
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class ExplanationBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(BottomSheetExplanationBinding::bind)
    private val title: String by lazy {
        arguments?.getString(TITLE_EXTRA)
            ?: throw IllegalArgumentException("No text supplied for ${javaClass.simpleName}")
    }
    private val markDownText: String by lazy {
        arguments?.getString(TEXT_EXTRA)
            ?: throw IllegalArgumentException("No text supplied for ${javaClass.simpleName}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_explanation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.title.text = title
        binding.body.setMarkdownText(markDownText)
    }

    companion object {
        const val TAG = "charity_explanation_bottom_sheet"
        private const val TITLE_EXTRA = "title"
        private const val TEXT_EXTRA = "text"

        fun newInstance(
            title: String,
            markDownText: String,
        ): ExplanationBottomSheet {
            val bottomSheet = ExplanationBottomSheet()
            bottomSheet.arguments = Bundle().apply {
                putString(TITLE_EXTRA, title)
                putString(TEXT_EXTRA, markDownText)
            }
            return bottomSheet
        }
    }
}
