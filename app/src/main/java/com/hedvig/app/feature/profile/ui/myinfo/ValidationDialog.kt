package com.hedvig.app.feature.profile.ui.myinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.hedvig.app.R
import kotlinx.android.synthetic.main.dialog_validation.*

class ValidationDialog : androidx.fragment.app.DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.dialog_validation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            dialogTitle.text = resources.getString(args.getInt(TITLE))
            dialogParagraph.text = resources.getString(args.getInt(PARAGRAPH))
            dialogConfirm.text = resources.getString(args.getInt(DISMISS))

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
        ): ValidationDialog {
            val dialog = ValidationDialog()
            val arguments = Bundle()
            arguments.apply {
                putInt(TITLE, title)
                putInt(PARAGRAPH, paragraph)
                putInt(DISMISS, dismiss)
            }
            dialog.arguments = arguments
            return dialog
        }
    }
}
