package com.hedvig.app.feature.embark.passages.numberaction

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.NumberActionFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NumberActionFragment : Fragment(R.layout.number_action_fragment) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(NumberActionFragmentBinding::bind)
    private val data: NumberActionParams
        get() = requireArguments().getParcelable(PARAMS)
            ?: throw Error("Programmer error: No PARAMS provided to ${this.javaClass.name}")
    private val numberActionViewModel: NumberActionViewModel by viewModel { parametersOf(data) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            messages.adapter = MessageAdapter(data.messages)
            inputContainer.placeholderText = data.placeholder
            data.label?.let { inputContainer.hint = it }
            data.unit?.let { inputContainer.helperText = it }
            input.doOnTextChanged { text, _, _, _ ->
                numberActionViewModel.validate(text)
            }
            numberActionViewModel.valid.observe(viewLifecycleOwner) { submit.isEnabled = it }
            submit.text = data.submitLabel
            submit.setHapticClickListener {
                model.putInStore(data.key, input.text.toString())
                model.navigateToPassage(data.link)
            }
        }
    }

    companion object {
        private const val PARAMS = "PARAMS"
        fun newInstance(params: NumberActionParams) = NumberActionFragment().apply {
            arguments = bundleOf(
                PARAMS to params
            )
        }
    }
}
