package com.hedvig.app.feature.offer

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.show
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.DateFormatSymbols
import java.util.Calendar

class ChangeDateBottomSheet : RoundedBottomSheetDialogFragment() {

    private val offerViewModel: OfferViewModel by viewModel()

    private lateinit var dateText: TextView
    private lateinit var dateHint: TextView
    private lateinit var autoSetDateTextView: TextView

    private var isDatePicked = false
    private lateinit var localDate: LocalDate

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.dialog_change_start_date)

        dateText = dialog.findViewById(R.id.dateText)
        dateHint = dialog.findViewById(R.id.dateHint)
        autoSetDateTextView = dialog.findViewById(R.id.autoSetDateText)

        dialog.findViewById<View>(R.id.datePickButton).setOnClickListener {
            showDatePickerDialog()
        }

        offerViewModel.data.observe(this) { d ->
            d?.let { data ->
                lateinit var buttonText: String
                data.lastQuoteOfMember?.completeQuote?.id?.let { id ->
                    dialog.findViewById<Button>(R.id.chooseDateButton).setOnClickListener {
                        if (isDatePicked) {
                            AlertDialog.Builder(context)
                                .setTitle("Är du säker?")
                                .setMessage("Om du väljer ditt eget startdatum behöver du själv säga upp din gamla försäkring så att allt går rätt till.")
                                .setPositiveButton(
                                    "Ja, välj datum"
                                ) { dialog, which ->
                                    offerViewModel.chooseStartDate(id, localDate)
                                    this.dialog?.hide()
                                }
                                .setNegativeButton("Ångra", null)
                                .show()
                        } else {
                            context?.makeToast("you have to pick a date", Toast.LENGTH_SHORT)
                        }
                    }
                }
                if (data.lastQuoteOfMember?.completeQuote?.currentInsurer == null) {

                    buttonText = "Activate today"
                    autoSetDateTextView.text = buttonText
                } else {

                    buttonText = "Aktivera när min gamla försäkring går ut"
                    autoSetDateTextView.text = buttonText
                }

                autoSetDateTextView.setOnClickListener {
                    //TODO
                    dialog.hide()
                }
            }
        }

        return dialog
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this.context!!,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                localDate = LocalDate.parse("$dayOfMonth/${monthOfYear + 1}/$year", formatter)
                val month= DateFormatSymbols().months[monthOfYear].capitalize()

                isDatePicked = true

                dateText.text = "$dayOfMonth $month $year"
                animateHintMove()
                dateText.show()
            },
            year,
            month,
            day
        )

        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()
    }

    private fun animateHintMove() {
        ObjectAnimator.ofFloat(dateHint, "translationY", -65f).apply {
            duration = 100
            start()
        }
    }

    companion object {

        const val TAG = "changeDateBottomSheet"

        fun newInstance(): ChangeDateBottomSheet {
            return ChangeDateBottomSheet()
        }

        val OfferQuery.LastQuoteOfMember.completeQuote: OfferQuery.AsCompleteQuote?
            get() = (this as? OfferQuery.AsCompleteQuote)
    }
}
