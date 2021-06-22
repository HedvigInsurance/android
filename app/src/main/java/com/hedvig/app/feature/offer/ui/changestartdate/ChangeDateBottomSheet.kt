package com.hedvig.app.feature.offer.ui.changestartdate

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogChangeStartDateBinding
import com.hedvig.app.util.extensions.epochMillisToLocalDate
import com.hedvig.app.util.extensions.repeatOnViewLifeCycleLaunch
import com.hedvig.app.util.extensions.showAlert
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChangeDateBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(DialogChangeStartDateBinding::bind)
    private val changeDateBottomSheetViewModel: ChangeDateBottomSheetViewModel by viewModel {
        val data = requireArguments().getParcelable<ChangeDateBottomSheetData>(DATA)
            ?: throw IllegalArgumentException("No data provided to ChangeDateBottomSheet")
        parametersOf(data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_change_start_date, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repeatOnViewLifeCycleLaunch {
            changeDateBottomSheetViewModel.viewState.collect { viewState ->
                when (viewState) {
                    ChangeDateBottomSheetViewModel.ViewState.Dismiss -> dismiss()
                    is ChangeDateBottomSheetViewModel.ViewState.Inceptions -> viewState.inceptions.forEach {
                        val changeDateView = createChangeDateView(it)
                        binding.changeDateContainer.addView(changeDateView)
                    }
                    ChangeDateBottomSheetViewModel.ViewState.ShowConfirmationDialog -> {
                        showLoadingState(false)
                        showChooseOwnStartDateDialog()
                    }
                    is ChangeDateBottomSheetViewModel.ViewState.Error -> {
                        showLoadingState(false)
                        requireContext().showAlert(
                            title = getString(R.string.error_dialog_title),
                            // TODO: Create general try again text?
                            message = viewState.message ?: getString(R.string.component_error),
                            positiveLabel = R.string.insurances_tab_error_button_text,
                            positiveAction = { changeDateBottomSheetViewModel.onDialogConfirmed() }
                        )
                    }
                    is ChangeDateBottomSheetViewModel.ViewState.Loading -> {
                        showLoadingState(viewState.showLoading)
                    }
                }

                binding.chooseDateButton.setOnClickListener {
                    changeDateBottomSheetViewModel.onChooseDateClicked()
                }
            }
        }
    }

    private fun createChangeDateView(inception: ChangeDateBottomSheetData.Inception): View {
        val changeDateView = ChangeDateView(requireContext())
        changeDateView.bind(
            title = if (!inception.isConcurrent) {
                inception.title
            } else null,
            startDate = inception.startDate,
            switchable = inception.currentInsurer?.switchable ?: false,
            datePickerListener = {
                MaterialDatePicker.Builder
                    .datePicker()
                    .setTitleText("")
                    .build()
                    .apply {
                        addOnPositiveButtonClickListener { epochMillis ->
                            changeDateView.setDateText(epochMillis.epochMillisToLocalDate())
                            changeDateBottomSheetViewModel.onDateSelected(
                                isConcurrent = inception.isConcurrent,
                                quoteId = inception.quoteId,
                                epochMillis = epochMillis
                            )
                        }
                    }
                    .show(childFragmentManager, DATE_PICKER_TAG)
            },
            switchListener = { checked ->
                changeDateBottomSheetViewModel.onSwitchChecked(inception.quoteId, checked)
            }
        )
        return changeDateView
    }

    private fun showChooseOwnStartDateDialog() {
        requireContext().showAlert(
            R.string.ALERT_TITLE_STARTDATE,
            R.string.ALERT_DESCRIPTION_STARTDATE,
            R.string.ALERT_CONTINUE,
            R.string.ALERT_CANCEL,
            { changeDateBottomSheetViewModel.onDialogConfirmed() }
        )
    }

    fun showLoadingState(isLoading: Boolean) {
        TransitionManager.beginDelayedTransition(binding.root)
        binding.chooseDateButton.isEnabled = !isLoading
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
