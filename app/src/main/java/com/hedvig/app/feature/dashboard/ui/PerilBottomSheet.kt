package com.hedvig.app.feature.dashboard.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.DrawableRes
import android.view.LayoutInflater
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.compatDrawable
import kotlinx.android.synthetic.main.peril_bottom_sheet.*

class PerilBottomSheet : RoundedBottomSheetDialogFragment() {

    override fun getTheme() = R.style.PerilBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.peril_bottom_sheet, null)
        dialog.setContentView(view)
        arguments?.let { args ->
            dialog.perilSubject.text = args.getString(PERIL_SUBJECT)
            dialog.perilIcon.setImageDrawable(requireContext().compatDrawable(args.getInt(PERIL_ICON)))

            // FIXME This old hack needs to die, but there's really no better way right now
            val title = args.getString(PERIL_TITLE)
            title?.let { dialog.perilTitle.text = if (it.contains("-\n")) it.replace("-\n", "") else it }

            dialog.perilDescription.text = args.getString(PERIL_DESCRIPTION)
        }
        return dialog
    }

    companion object {
        private const val PERIL_SUBJECT = "peril_subject"
        private const val PERIL_ICON = "peril_icon"
        private const val PERIL_TITLE = "peril_title"
        private const val PERIL_DESCRIPTION = "peril_description"

        const val TAG = "perilBottomSheet"

        fun newInstance(subject: String, @DrawableRes icon: Int, title: String, description: String): PerilBottomSheet {
            val arguments = Bundle().apply {
                putString(PERIL_SUBJECT, subject)
                putInt(PERIL_ICON, icon)
                putString(PERIL_TITLE, title)
                putString(PERIL_DESCRIPTION, description)
            }

            return PerilBottomSheet().also { it.arguments = arguments }
        }
    }
}
