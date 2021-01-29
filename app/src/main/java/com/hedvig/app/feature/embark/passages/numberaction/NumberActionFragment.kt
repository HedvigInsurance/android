package com.hedvig.app.feature.embark.passages.numberaction

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.NumberActionFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import org.koin.android.viewmodel.ext.android.sharedViewModel

class NumberActionFragment : Fragment(R.layout.number_action_fragment) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(NumberActionFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val data = requireArguments().getParcelable<NumberActionParams>(PARAMS)
        if (data == null) {
            e { "Programmer error: No PARAMS provided to ${this.javaClass.name}" }
            return
        }
        with(binding) {
            inputContainer.placeholderText = data.placeholder
            data.unit?.let { unit.text = it }
            input.doOnTextChanged { text, _, _, _ ->
                submit.isEnabled = !text.isNullOrBlank()
            }
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
