package com.hedvig.app.feature.connectpayin

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.ConnectPaymentExplainerFragmentBinding
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.onBackPressedCallback
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PostSignExplainerFragment : Fragment(R.layout.connect_payment_explainer_fragment) {
  private val viewModel: ConnectPaymentViewModel by sharedViewModel()
  private val binding by viewBinding(ConnectPaymentExplainerFragmentBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val paymentType = requireArguments().getSerializable(PAYIN_TYPE) as? ConnectPayinType
    if (paymentType == null) {
      e { "Programmer error: PAYIN_TYPE not supplied to ${this.javaClass.name}" }
      return
    }

    requireActivity().onBackPressedDispatcher.addCallback(
      viewLifecycleOwner,
      onBackPressedCallback {
        showConfirmCloseDialog(requireContext(), paymentType, viewModel::close)
      },
    )

    binding.apply {
      when (paymentType) {
        ConnectPayinType.TRUSTLY -> {
          explainerTitle.setText(hedvig.resources.R.string.pay_in_explainer_direct_debit_headline)
          explainerButton.setText(hedvig.resources.R.string.pay_in_explainer_direct_debit_button_text)
        }
        ConnectPayinType.ADYEN -> {
          explainerTitle.setText(hedvig.resources.R.string.pay_in_explainer_headline)
          explainerButton.setText(hedvig.resources.R.string.pay_in_explainer_button_text)
        }
      }
      explainerButton.setHapticClickListener {
        viewModel.navigateTo(ConnectPaymentScreenState.Connect(TransitionType.ENTER_LEFT_EXIT_RIGHT))
      }
    }

    viewModel.readyToStart.observe(viewLifecycleOwner) { binding.explainerButton.isEnabled = it }
  }

  companion object {
    private const val PAYIN_TYPE = "PAYIN_TYPE"
    fun newInstance(payinType: ConnectPayinType) = PostSignExplainerFragment().apply {
      arguments = bundleOf(PAYIN_TYPE to payinType)
    }
  }
}
