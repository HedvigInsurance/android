package com.hedvig.app.ui.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.dialog_year_month_picker.*
import kotlinx.android.synthetic.main.dialog_year_month_picker.view.*
import org.threeten.bp.YearMonth
import java.text.DateFormatSymbols
import java.util.Calendar

abstract class YearMonthPickerDialog : DialogFragment() {
    abstract val title: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_year_month_picker, null)
        val today = Calendar.getInstance()

        view.month.apply {
            minValue = 1
            maxValue = 12
            value = savedInstanceState?.getInt(MONTH) ?: today.get(Calendar.MONTH) + 1
            displayedValues = DateFormatSymbols().months
            wrapSelectorWheel = false
        }

        view.year.apply {
            minValue = 0
            maxValue = today.get(Calendar.YEAR)
            value = savedInstanceState?.getInt(YEAR) ?: today.get(Calendar.YEAR)
            wrapSelectorWheel = false
        }

        view.title.text = title

        view.ok.setHapticClickListener {
            onSubmit(YearMonth.of(view.year.value, view.month.value))
        }

        view.cancel.setHapticClickListener {
            onCancel()
        }

        builder.setView(view)

        val dialog = builder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        dialog?.month?.value?.let { month ->
            outState.putInt(MONTH, month)
        }

        dialog?.year?.value?.let { year ->
            outState.putInt(YEAR, year)
        }

        super.onSaveInstanceState(outState)
    }

    abstract fun onSubmit(yearMonth: YearMonth)

    private fun onCancel() {
        dismiss()
    }

    companion object {
        private const val YEAR = "YEAR"
        private const val MONTH = "MONTH"
    }
}
