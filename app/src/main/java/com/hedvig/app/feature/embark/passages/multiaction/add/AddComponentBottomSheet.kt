package com.hedvig.app.feature.embark.passages.multiaction.add

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogAddBuildingBinding
import com.hedvig.app.feature.embark.passages.multiaction.ComponentState
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.util.extensions.view.disable
import com.hedvig.app.util.extensions.view.enable
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.android.synthetic.main.dialog_add_building.numberLayout
import kotlinx.android.synthetic.main.picker_button.continueButton
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AddComponentBottomSheet : BottomSheetDialogFragment() {

    private val componentState: ComponentState? by lazy {
        requireArguments().getParcelable(COMPONENT_STATE)
    }

    private val multiActionParams: MultiActionParams by lazy {
        requireArguments().getParcelable<MultiActionParams>(MULTI_ACTION_PARAMS)
            ?: throw Error("Programmer error: No multi action params provided to ${this.javaClass.name}")
    }

    private val viewModel: AddComponentViewModel by viewModel { parametersOf(multiActionParams, componentState) }
    private val binding by viewBinding(DialogAddBuildingBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_add_building, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            val component = multiActionParams.components.first()
            val dropDownOptions = component.dropdown?.options?.map { it.text } ?: emptyList()

            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.dropdown_menu_popup_item,
                dropDownOptions)

            dropdownLayout.hint = component.dropdown?.label

            dropdownInput.setAdapter(adapter)
            dropdownInput.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.dropDownSelection.value = dropDownOptions[position]
            }

            componentSwitch.hint = component.switch?.label
            componentSwitch.isEnabled = component.switch?.defaultValue ?: false
            componentSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.switchSelection.value = isChecked
            }

            numberLayout.hint = component.number?.label
            numberInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let {
                        viewModel.input.value = s.toString()
                    }
                }
            })

            componentState?.let { state ->
                dropdownInput.setText(state.dropDownSelection, false)
                numberInput.setText(state.input)
                componentSwitch.isChecked = state.switch
                continueButton.isEnabled = true
            }
        }

        viewModel.viewState.observe(this) {
            continueButton.isEnabled = it is AddComponentViewModel.ViewState.Valid

            when (it) {
                AddComponentViewModel.ViewState.Valid,
                AddComponentViewModel.ViewState.NoSelection -> numberLayout.error = null
                AddComponentViewModel.ViewState.Error.MaxInput -> numberLayout.error = "Max input"
                AddComponentViewModel.ViewState.Error.MinInput -> numberLayout.error = "Min input"
                AddComponentViewModel.ViewState.Error.NoInput -> numberLayout.error = "Required field"
            }
        }

        binding.continueButton.setOnClickListener {
            viewModel.onContinue()
        }

        viewModel.componentResultEvent.observe(this) {
            val bundle = Bundle().apply { putParcelable(RESULT, it) }
            setFragmentResult(ADD_COMPONENT_REQUEST_KEY, bundle)
            dismiss()
        }
    }

    companion object {
        private const val COMPONENT_STATE = "COMPONENT_STATE"
        private const val MULTI_ACTION_PARAMS = "MULTI_ACTION_PARAMS"
        const val RESULT = "RESULT"

        const val ADD_COMPONENT_REQUEST_KEY = "ADD_COMPONENT"
        const val TAG = "changeDateBottomSheet"

        fun newInstance(
            component: ComponentState?,
            multiActionParams: MultiActionParams
        ) = AddComponentBottomSheet().apply {
            arguments = bundleOf(
                COMPONENT_STATE to component,
                MULTI_ACTION_PARAMS to multiActionParams
            )
        }
    }
}
