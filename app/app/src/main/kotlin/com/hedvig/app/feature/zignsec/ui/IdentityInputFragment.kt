package com.hedvig.app.feature.zignsec.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.android.market.Market
import com.hedvig.app.R
import com.hedvig.app.databinding.IdentityInputFragmentBinding
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationData
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.hedvig.app.util.extensions.onImeAction
import com.hedvig.app.util.extensions.setHelperText
import com.hedvig.app.util.extensions.setMaxLength
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.parameter.parametersOf

class IdentityInputFragment : Fragment(R.layout.identity_input_fragment) {
  private val binding by viewBinding(IdentityInputFragmentBinding::bind)
  private val viewModel: SimpleSignAuthenticationViewModel by activityViewModel { parametersOf(data) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
  }

  private val data by lazy {
    requireArguments().parcelable<SimpleSignAuthenticationData>(DATA)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    with(binding) {
      when (data?.market) {
        Market.NO -> {
          input.setHint(hedvig.resources.R.string.simple_sign_login_text_field_label)
          input.setHelperText(hedvig.resources.R.string.simple_sign_login_text_field_helper_text)
          inputText.setMaxLength(11)
          signIn.setText(hedvig.resources.R.string.simple_sign_sign_in)
        }
        Market.DK -> {
          input.setHint(hedvig.resources.R.string.simple_sign_login_text_field_label_dk)
          input.setHelperText(hedvig.resources.R.string.simple_sign_login_text_field_helper_text_dk)
          inputText.setMaxLength(10)
          signIn.setText(hedvig.resources.R.string.simple_sign_sign_in_dk)
        }
        else -> {
          viewModel.invalidMarket()
          return
        }
      }

      inputText.apply {
        requestFocus()
        doOnTextChanged { text, _, _, _ -> viewModel.setInput(text) }
        onImeAction { startZignSecIfValid() }
      }
      viewModel.isValid.observe(viewLifecycleOwner) { isValid ->
        if (viewModel.isSubmitting.value != true) {
          signIn.isEnabled = isValid
        }
      }
      viewModel.isSubmitting.observe(viewLifecycleOwner) { isSubmitting ->
        if (isSubmitting) {
          signIn.isEnabled = false
        }
      }

      signIn.setHapticClickListener {
        startZignSecIfValid()
      }
    }
  }

  private fun startZignSecIfValid() {
    if (viewModel.isValid.value == true) {
      viewModel.startZignSec()
    }
  }

  companion object {

    private const val DATA = "DATA"
    fun newInstance(data: SimpleSignAuthenticationData) = IdentityInputFragment().apply {
      arguments = bundleOf(DATA to data)
    }
  }
}
