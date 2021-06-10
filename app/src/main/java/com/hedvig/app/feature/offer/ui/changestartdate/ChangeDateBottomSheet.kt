package com.hedvig.app.feature.offer.ui.changestartdate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogChangeStartDateBinding
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.util.extensions.isToday
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChangeDateBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(DialogChangeStartDateBinding::bind)
    private val offerViewModel: OfferViewModel by sharedViewModel()
    private val changeDateBottomSheetViewModel: ChangeDateBottomSheetViewModel by viewModel {
        val data = requireArguments().getParcelable<ChangeDateBottomSheetData>(DATA)
            ?: throw IllegalArgumentException("No data provided to ChangeDateBottomSheet")
        parametersOf(data)
    }
    private val tracker: OfferTracker by inject()

    private val dateFormat = DateTimeFormatter.ofPattern("d/M/yyyy")
    private val datePickerDialog = MaterialDatePicker.Builder
        .datePicker()
        .setTitleText("")
        .build()
        .apply { addClickListener() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_change_start_date, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            changeDateBottomSheetViewModel.viewState.collect { viewState ->
                val dateText = viewState.getFormattedDateText()
                binding.datePickText.setText(dateText)

                binding.autoSetDateSwitch.isVisible = viewState.hasSwitchableInsurer
                binding.autoSetDateTitle.isVisible = viewState.hasSwitchableInsurer
                if (viewState.hasSwitchableInsurer) {
                    setupDateSwitch(viewState, dateText)
                }
                setupChooseDateButton(viewState)
            }
        }

        binding.datePickText.setHapticClickListener {
            showDatePickerDialog()
        }
    }

    private fun setupChooseDateButton(viewState: ChangeDateBottomSheetViewModel.ViewState) {
        binding.chooseDateButton.setOnClickListener {
            val localDate = viewState.selectedDateTime.toLocalDate()
            if (viewState.selectedDateTime.isToday()) {
                setDateAndFinish(viewState.id, localDate)
            } else {
                showChooseOwnStartDateDialog(viewState.id, localDate)
            }
        }
    }

    private fun setupDateSwitch(
        viewState: ChangeDateBottomSheetViewModel.ViewState,
        dateText: String?
    ) {
        binding.autoSetDateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tracker.activateOnInsuranceEnd()
                offerViewModel.removeStartDate(viewState.id)
                binding.datePickText.text = null
            } else {
                binding.datePickText.setText(dateText)
            }

            binding.datePickText.isEnabled = !isChecked
            binding.datePickLayout.isEnabled = !isChecked
        }
    }

    private fun ChangeDateBottomSheetViewModel.ViewState.getFormattedDateText() =
        if (selectedDateTime.isToday()) {
            getString(R.string.START_DATE_TODAY)
        } else {
            selectedDateTime.format(dateFormat)
        }

    private fun showChooseOwnStartDateDialog(id: String, date: LocalDate) {
        requireContext().showAlert(
            R.string.ALERT_TITLE_STARTDATE,
            R.string.ALERT_DESCRIPTION_STARTDATE,
            R.string.ALERT_CONTINUE,
            R.string.ALERT_CANCEL,
            { setDateAndFinish(id, date) }
        )
    }

    private fun setDateAndFinish(id: String, date: LocalDate) {
        tracker.changeDateContinue()
        offerViewModel.chooseStartDate(id, date)
        dismiss()
    }

    private fun showDatePickerDialog() {
        datePickerDialog.show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun MaterialDatePicker<Long>.addClickListener() {
        addOnPositiveButtonClickListener { epochMillis ->
            changeDateBottomSheetViewModel.onDateSelected(epochMillis)
        }
    }

    companion object {
        private const val DATA = "DATA"
        private const val DATE_PICKER_TAG = "DATE_PICKER_TAG"
        const val TAG = "changeDateBottomSheet"

        fun newInstance(data: ChangeDateBottomSheetData): ChangeDateBottomSheet {
            return ChangeDateBottomSheet()
                .apply {
                    arguments = bundleOf(DATA to data)
                }
        }
    }
}
