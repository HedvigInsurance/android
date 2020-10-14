package com.hedvig.app.feature.connectpayin

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.ConnectPaymentExplainerFragmentBinding
import com.hedvig.app.feature.trustly.onBackPressedCallback
import com.hedvig.app.feature.trustly.showConfirmCloseDialog
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PostSignExplainerFragment : Fragment(R.layout.connect_payment_explainer_fragment) {
    private val model: ConnectPaymentViewModel by sharedViewModel()
    private val binding by viewBinding(ConnectPaymentExplainerFragmentBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isPostSign = requireArguments().getBoolean(IS_POST_SIGN)

        if (isPostSign) {
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                onBackPressedCallback({
                    showConfirmCloseDialog(requireContext(), model::close)
                })
            )
        }

        binding.explainerButton.setHapticClickListener {
            model.navigateTo(ConnectPaymentScreenState.Connect(TransitionType.ENTER_LEFT_EXIT_RIGHT))
        }

        model.readyToStart.observe(viewLifecycleOwner) { binding.explainerButton.isEnabled = it }
    }

    companion object {
        private const val IS_POST_SIGN = "IS_POST_SIGN"
        fun newInstance(isPostSign: Boolean = false) = PostSignExplainerFragment().apply {
            arguments = bundleOf(IS_POST_SIGN to isPostSign)
        }
    }
}

