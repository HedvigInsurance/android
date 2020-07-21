package com.hedvig.app.feature.offer

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.dialog_change_start_date.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ChangeDateBottomSheet : BottomSheetDialogFragment() {

    private val offerViewModel: OfferViewModel by viewModel()
    private val tracker: OfferTracker by inject()

    private lateinit var localDate: LocalDate

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.dialog_change_start_date)

        dialog.datePickButton.setHapticClickListener {
            showDatePickerDialog()
        }

        dialog.chooseDateButton.isEnabled = false

        offerViewModel.data.observe(this) { d ->
            d?.let { data ->
                data.lastQuoteOfMember.asCompleteQuote?.id?.let { id ->
                    dialog.chooseDateButton.setOnClickListener {
                        requireContext().showAlert(R.string.ALERT_TITLE_STARTDATE,
                            R.string.ALERT_DESCRIPTION_STARTDATE,
                            R.string.ALERT_CONTINUE,
                            R.string.ALERT_CANCEL,
                            {
                                tracker.changeDateContinue()
                                offerViewModel.chooseStartDate(id, localDate)
                                dialog.hide()
                            })
                    }
                    if (data.lastQuoteOfMember.asCompleteQuote?.currentInsurer?.switchable == true) {
                        dialog.autoSetDateText.text = getString(R.string.ACTIVATE_INSURANCE_END_BTN)

                        dialog.autoSetDateText.setHapticClickListener {
                            tracker.activateOnInsuranceEnd()
                            offerViewModel.removeStartDate(id)
                            dialog.hide()
                        }
                    } else {
                        dialog.autoSetDateText.text = getString(R.string.ACTIVATE_TODAY_BTN)

                        dialog.autoSetDateText.setHapticClickListener {
                            tracker.activateToday()
                            offerViewModel.chooseStartDate(id, LocalDate.now())
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
        val defaultYear = c.get(Calendar.YEAR)
        val defaultMonth = c.get(Calendar.MONTH)
        val defaultDay = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                localDate = LocalDate.parse("$dayOfMonth/${monthOfYear + 1}/$year", formatter)
                val monthFormatted = DateFormatSymbols().months[monthOfYear].capitalize()

                dialog?.datePickButton?.text = "$dayOfMonth $monthFormatted $year"

                dialog?.chooseDateButton?.isEnabled = true
            },
            defaultYear,
            defaultMonth,
            defaultDay
        )

        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()
    }

    companion object {

        const val TAG = "changeDateBottomSheet"

        fun newInstance(): ChangeDateBottomSheet {
            return ChangeDateBottomSheet()
        }
    }
}
