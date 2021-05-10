package com.hedvig.app.feature.embark.passages.multiaction.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogAddBuildingBinding
import com.hedvig.app.databinding.LayoutComponentDropdownBinding
import com.hedvig.app.databinding.LayoutComponentNumberBinding
import com.hedvig.app.databinding.LayoutComponentSwitchBinding
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionComponent
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.util.extensions.hideKeyboard
import com.hedvig.app.util.extensions.onImeAction
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AddComponentBottomSheet : BottomSheetDialogFragment() {

    private val componentState: MultiActionItem.Component? by lazy {
        requireArguments().getParcelable(COMPONENT_STATE)
    }

    private val multiActionParams: MultiActionParams by lazy {
        requireArguments().getParcelable<MultiActionParams>(MULTI_ACTION_PARAMS)
            ?: throw Error("Programmer error: No multi action params provided to ${this.javaClass.name}")
    }

    private val viewModel: AddComponentViewModel by viewModel { parametersOf(componentState, multiActionParams) }
    private val binding by viewBinding(DialogAddBuildingBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_add_building, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        multiActionParams.components.map { component ->
            val componentView = when (component) {
                is MultiActionComponent.Dropdown -> createDropDownComponent(component)
                is MultiActionComponent.Number -> createNumberComponent(component)
                is MultiActionComponent.Switch -> createSwitchComponent(component)
            }

            binding.componentContainer.addView(componentView)
        }

        viewModel.viewState.observe(this) {
            binding.continueButton.isEnabled = it is AddComponentViewModel.ViewState.Valid
        }

        binding.continueButton.isEnabled = componentState != null
        binding.continueButton.setOnClickListener {
            viewModel.onContinue()
        }

        viewModel.componentResultEvent.observe(this) {
            val bundle = Bundle().apply { putParcelable(RESULT, it) }
            setFragmentResult(ADD_COMPONENT_REQUEST_KEY, bundle)
            dismiss()
        }
    }

    private fun createDropDownComponent(dropdown: MultiActionComponent.Dropdown): TextInputLayout {
        return LayoutComponentDropdownBinding.inflate(LayoutInflater.from(requireContext()), binding.componentContainer, false).apply {
            val dropDownOptions = dropdown.options.map { it.text }
            val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, dropDownOptions)

            dropdownLayout.hint = dropdown.label
            dropdownInput.setAdapter(adapter)
            dropdownInput.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.onDropDownChanged(dropdown.key, dropDownOptions[position])
            }

            componentState?.selectedDropDowns?.firstOrNull { it.key == dropdown.key }?.let {
                dropdownInput.setText(it.value, false)
                viewModel.onDropDownChanged(dropdown.key, it.value)
            }
        }.root
    }

    private fun createNumberComponent(number: MultiActionComponent.Number): TextInputLayout {
        return LayoutComponentNumberBinding.inflate(LayoutInflater.from(requireContext()), binding.componentContainer, false).apply {
            numberLayout.hint = number.label
            numberInput.onImeAction {
                requireContext().hideKeyboard(numberInput)
            }

            numberInput.doOnTextChanged { text, _, _, _ ->
                viewModel.onNumberChanged(number.key, text.toString(), number.unit)
            }

            viewModel.inputsViewState.observe(viewLifecycleOwner) {
                when (it[number.key]) {
                    AddComponentViewModel.NumberState.Error.MaxInput -> numberLayout.error = "Max input"
                    AddComponentViewModel.NumberState.Error.MinInput -> numberLayout.error = "Min input"
                    AddComponentViewModel.NumberState.NoInput,
                    is AddComponentViewModel.NumberState.Valid,
                    null -> numberLayout.error = null
                }
            }

            componentState?.inputs?.firstOrNull { it.key == number.key }?.let {
                numberInput.setText(it.value)
            }
        }.root
    }

    private fun createSwitchComponent(switch: MultiActionComponent.Switch): SwitchCompat {
        return LayoutComponentSwitchBinding.inflate(LayoutInflater.from(requireContext()), binding.componentContainer, false).apply {
            componentSwitch.text = switch.label
            componentSwitch.isChecked = switch.defaultValue
            componentSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onSwitchChanged(switch.key, isChecked, switch.label)
            }

            componentState?.switches?.firstOrNull { it.key == switch.key }?.let {
                componentSwitch.isChecked = it.value
            }
        }.root
    }

    companion object {
        private const val COMPONENT_STATE = "COMPONENT_STATE"
        private const val MULTI_ACTION_PARAMS = "MULTI_ACTION_PARAMS"
        const val RESULT = "RESULT"

        const val ADD_COMPONENT_REQUEST_KEY = "ADD_COMPONENT"
        const val TAG = "changeDateBottomSheet"

        fun newInstance(
            component: MultiActionItem.Component?,
            multiActionParams: MultiActionParams
        ) = AddComponentBottomSheet().apply {
            arguments = bundleOf(
                COMPONENT_STATE to component,
                MULTI_ACTION_PARAMS to multiActionParams
            )
        }
    }
}
