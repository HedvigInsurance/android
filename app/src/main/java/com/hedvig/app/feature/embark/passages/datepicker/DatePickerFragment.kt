package com.hedvig.app.feature.embark.passages.datepicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkDatePickerBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.feature.embark.masking.SHORT_DATE
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.IllegalArgumentException
import java.time.format.DateTimeFormatter

class DatePickerFragment : Fragment(R.layout.fragment_embark_date_picker) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val datePickerViewModel: DatePickerViewModel by viewModel()
    private val binding by viewBinding(FragmentEmbarkDatePickerBinding::bind)
    private val data: DatePickerParams
        get() = requireArguments().getParcelable(DATA)
            ?: throw Error("Programmer error: No PARAMS provided to ${this.javaClass.name}")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            messages.adapter = MessageAdapter(data.messages)

            dateContainer.setOnClickListener {
                datePickerViewModel.onShowDatePicker()
            }

            continueButton.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
            }

            continueButton
                .hapticClicks()
                .mapLatest { saveAndAnimate() }
                .onEach { model.submitAction(data.link) }
                .launchIn(viewLifecycleScope)
        }

        datePickerViewModel.selectedDate.observe(viewLifecycleOwner) { selectedDate ->
            binding.continueButton.isEnabled = selectedDate != null
            binding.dateLabel.text = if (selectedDate != null) {
                selectedDate.format(SHORT_DATE)
            } else {
                data.placeholder
            }
        }

        datePickerViewModel.showDatePicker.observe(viewLifecycleOwner) { selectedDate ->
            MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("")
                .apply { if (selectedDate != null) setSelection(selectedDate) }
                .build()
                .apply { addOnPositiveButtonClickListener { datePickerViewModel.onDateSelected(it) } }
                .show(childFragmentManager, DATE_PICKER_TAG)
        }
    }

    private suspend fun saveAndAnimate() {
        val date = datePickerViewModel.selectedDate.value?.format(DateTimeFormatter.ISO_DATE)
            ?: throw IllegalArgumentException("No date selected when trying to continue")
        val inputText = binding.dateLabel.text.toString()
        model.putInStore("${data.passageName}Result", inputText)
        model.putInStore(data.storeKey, date)
        val response = model.preProcessResponse(data.passageName) ?: Response.SingleResponse(inputText)
        animateResponse(binding.responseContainer, response)
    }

    companion object {
        private const val DATA = "DATA"
        private const val DATE_PICKER_TAG = "datePicker"

        fun newInstance(data: DatePickerParams) =
            DatePickerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, data)
                }
            }
    }
}
