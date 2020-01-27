package com.hedvig.app.feature.offer

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import com.airbnb.paris.extensions.style
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.date_pick_layout.*
import kotlinx.android.synthetic.main.dialog_change_start_date.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.DateFormatSymbols
import java.util.Calendar

class ChangeDateBottomSheet : RoundedBottomSheetDialogFragment() {

    private val offerViewModel: OfferViewModel by viewModel()

    private var isDatePicked = false
    private lateinit var localDate: LocalDate

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.dialog_change_start_date)

        dialog.datePickButton.setOnClickListener {
            showDatePickerDialog()
        }

        dialog.chooseDateButton.isEnabled = false

        offerViewModel.data.observe(this) { d ->
            d?.let { data ->
                lateinit var buttonText: String
                data.lastQuoteOfMember?.completeQuote?.id?.let { id ->
                    dialog.chooseDateButton.setOnClickListener {
                        if (isDatePicked) {
                            AlertDialog.Builder(context)
                                //TODO
                                .setTitle("Är du säker?")
                                //TODO
                                .setMessage("Om du väljer ditt eget startdatum behöver du själv säga upp din gamla försäkring så att allt går rätt till.")
                                //TODO
                                .setPositiveButton(
                                    //TODO
                                    "Ja, välj datum"
                                ) { dialog, which ->
                                    offerViewModel.chooseStartDate(id, localDate)
                                    this.dialog?.hide()
                                }
                                //TODO
                                .setNegativeButton("Ångra", null)
                                .show()
                        }
                    }
                    if (data.lastQuoteOfMember?.completeQuote?.currentInsurer == null) {
                        //TODO
                        buttonText = "Activate today"
                        dialog.autoSetDateText.text = buttonText

                        dialog.autoSetDateText.setOnClickListener {
                            offerViewModel.chooseStartDate(id, LocalDate.now())
                            dialog.hide()
                        }
                    } else {
                        //TODO
                        buttonText = "Aktivera när min gamla försäkring går ut"
                        dialog.autoSetDateText.text = buttonText
                        dialog.autoSetDateText.setOnClickListener {
                            offerViewModel.removeStartDate(id)
                            dialog.hide()
                        }
                    }
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
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                localDate = LocalDate.parse("$dayOfMonth/${monthOfYear + 1}/$year", formatter)
                val month= DateFormatSymbols().months[monthOfYear].capitalize()

                isDatePicked = true

                dialog?.dateText?.text = "$dayOfMonth $month $year"
                animateHintMove()
                dialog?.dateText?.show()

                dialog?.chooseDateButton?.isEnabled = true
                dialog?.chooseDateButton?.style(R.style.HedvigButton_Purple)
            },
            year,
            month,
            day
        )

        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()
    }

    private fun animateHintMove() {
        val animateDistance = dialog?.datePickButton
            ?.height?.div(2.5)
            ?.toFloat() ?: return

        dialog?.dateHint?.let { dateHint ->
            dateHint.
                animate()
                .translationY(-animateDistance)
                .setDuration(100)
                .start()
        }
    }

    companion object {

        const val TAG = "changeDateBottomSheet"

        fun newInstance(): ChangeDateBottomSheet {
            return ChangeDateBottomSheet()
        }

        private val OfferQuery.LastQuoteOfMember.completeQuote: OfferQuery.AsCompleteQuote?
            get() = (this as? OfferQuery.AsCompleteQuote)
    }
}
