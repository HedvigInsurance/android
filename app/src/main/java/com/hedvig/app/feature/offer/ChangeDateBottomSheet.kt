package com.hedvig.app.feature.offer

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.spring
import kotlinx.android.synthetic.main.date_pick_layout.*
import kotlinx.android.synthetic.main.dialog_change_start_date.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.DateFormatSymbols
import java.util.Calendar

class ChangeDateBottomSheet : RoundedBottomSheetDialogFragment() {

    private val offerViewModel: OfferViewModel by viewModel()

    private lateinit var localDate: LocalDate

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.dialog_change_start_date)

        dialog.datePickButton.setOnClickListener {
            showDatePickerDialog()
        }

        dialog.chooseDateButton.isEnabled = false
        dialog.chooseDateButton.backgroundTintList =
            ColorStateList.valueOf(requireContext().compatColor(R.color.semi_light_gray))

        offerViewModel.data.observe(this) { d ->
            d?.let { data ->
                lateinit var buttonText: String
                data.lastQuoteOfMember?.completeQuote?.id?.let { id ->
                    if (data.insurance.previousInsurer != null) {
                        dialog.chooseDateButton.setOnClickListener {
                            requireContext().showAlert(R.string.ALERT_TITLE_STARTDATE,
                                R.string.ALERT_DESCRIPTION_STARTDATE,
                                R.string.ALERT_CONTINUE,
                                R.string.ALERT_CANCEL,
                                {
                                    offerViewModel.chooseStartDate(id, localDate)
                                    dialog.hide()
                                })
                        }
                    } else {
                        dialog.chooseDateButton.setOnClickListener {
                            offerViewModel.chooseStartDate(id, localDate)
                            dialog.hide()
                        }
                    }
                    if (data.lastQuoteOfMember?.completeQuote?.currentInsurer == null) {
                        buttonText = getString(R.string.ACTIVATE_TODAY_BTN)
                        dialog.autoSetDateText.text = buttonText

                        dialog.autoSetDateText.setOnClickListener {
                            offerViewModel.chooseStartDate(id, LocalDate.now())
                            dialog.hide()
                        }
                    } else {
                        buttonText = getString(R.string.ACTIVATE_INSURANCE_END_BTN)
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
                val month = DateFormatSymbols().months[monthOfYear].capitalize()

                dialog?.dateText?.text = "$dayOfMonth $month $year"
                animateHintMove()
                dialog?.dateText?.show()

                dialog?.chooseDateButton?.isEnabled = true
                dialog?.chooseDateButton?.backgroundTintList =
                    ColorStateList.valueOf(requireContext().compatColor(R.color.purple))
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
            dateHint.spring(SpringAnimation.TRANSLATION_Y, SpringForce.DAMPING_RATIO_LOW_BOUNCY, SpringForce.STIFFNESS_MEDIUM)
                .animateToFinalPosition(-animateDistance)
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
