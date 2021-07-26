package com.hedvig.app.feature.offer.ui.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FaqBottomSheetBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e

class FAQBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(FaqBottomSheetBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        inflater.inflate(R.layout.faq_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = requireArguments().getString(TITLE)
        val body = requireArguments().getString(BODY)

        if (title == null || body == null) {
            e { "Programmer error: TITLE or BODY not supplied to ${this.javaClass.name}" }
            return
        }

        binding.title.text = title
        binding.body.text = body
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val BODY = "BODY"

        fun newInstance(
            item: FAQItem
        ) = FAQBottomSheet().apply {
            arguments = bundleOf(
                TITLE to item.headline,
                BODY to item.body,
            )
        }

        const val TAG = "FAQBottomSheet"
    }
}
