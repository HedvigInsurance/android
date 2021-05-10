package com.hedvig.app.feature.profile.ui.myinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogValidationBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class ValidationDialog : DialogFragment() {
    private val binding by viewBinding(DialogValidationBinding::bind)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.dialog_validation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            dialogTitle.text = resources.getString(requireArguments().getInt(TITLE))
            dialogParagraph.text = resources.getString(requireArguments().getInt(PARAGRAPH))
            dialogConfirm.text = resources.getString(requireArguments().getInt(DISMISS))

            dialogConfirm.setOnClickListener {
                dialog?.dismiss()
            }
        }
    }

    companion object {
        const val TAG = "validation_dialog"

        private const val TITLE = "title"
        private const val PARAGRAPH = "paragraph"
        private const val DISMISS = "dismiss"

        fun newInstance(
            @StringRes title: Int,
            @StringRes paragraph: Int,
            @StringRes dismiss: Int
        ) = ValidationDialog().apply {
            arguments = bundleOf(
                TITLE to title,
                PARAGRAPH to paragraph,
                DISMISS to dismiss
            )
        }
    }
}
