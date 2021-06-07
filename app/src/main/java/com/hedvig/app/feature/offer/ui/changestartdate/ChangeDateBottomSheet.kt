package com.hedvig.app.feature.offer.ui.changestartdate

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogChangeStartDateBinding
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ChangeDateBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(DialogChangeStartDateBinding::bind)
    private val offerViewModel: OfferViewModel by sharedViewModel()
    private val tracker: OfferTracker by inject()

    private var localDate = LocalDate.now()
    private var formattedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_change_start_date, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            datePickText.setHapticClickListener {
                showDatePickerDialog()
            }

            val data = arguments?.getParcelable<ChangeDateBottomSheetData>(
                DATA
            )

            if (data == null) {
                e { "Programmer error: DATA not passed to ${this.javaClass.name}" }
                return
            }

            chooseDateButton.setOnClickListener {
                if (!localDate.isEqual(LocalDate.now())) {
                    requireContext().showAlert(
                        R.string.ALERT_TITLE_STARTDATE,
                        R.string.ALERT_DESCRIPTION_STARTDATE,
                        R.string.ALERT_CONTINUE,
                        R.string.ALERT_CANCEL,
                        {
                            setDateAndFinish(data)
                        }
                    )
                } else {
                    setDateAndFinish(data)
                }
            }

            autoSetDateSwitch.isVisible = data.hasSwitchableInsurer || true
            autoSetDateTitle.isVisible = data.hasSwitchableInsurer || true

            if (data.hasSwitchableInsurer || true) {
                autoSetDateSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        tracker.activateOnInsuranceEnd()
                        offerViewModel.removeStartDate(data.id)
                        datePickText.text = null
                    } else {
                        datePickText.setText(formattedDate)
                    }

                    datePickText.isEnabled = !isChecked
                    datePickLayout.isEnabled = !isChecked
                }
            }

            datePickText.setText(R.string.START_DATE_TODAY)
        }
    }

    private fun setDateAndFinish(data: ChangeDateBottomSheetData) {
        tracker.changeDateContinue()
        offerViewModel.chooseStartDate(data.id, localDate)
        dismiss()
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        val defaultYear = c.get(Calendar.YEAR)
        val defaultMonth = c.get(Calendar.MONTH)
        val defaultDay = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                localDate = LocalDate.parse("$dayOfMonth/${monthOfYear + 1}/$year", formatter)
                val monthFormatted = DateFormatSymbols().months[monthOfYear].capitalize(Locale.getDefault())
                formattedDate = "$dayOfMonth $monthFormatted $year"
                binding.datePickText.setText(formattedDate)
            },
            defaultYear,
            defaultMonth,
            defaultDay
        )

        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()
    }

    companion object {
        private const val DATA = "DATA"

        const val TAG = "changeDateBottomSheet"

        fun newInstance(data: ChangeDateBottomSheetData): ChangeDateBottomSheet {
            return ChangeDateBottomSheet()
                .apply {
                    arguments = bundleOf(
                        DATA to data
                    )
                }
        }
    }
}
