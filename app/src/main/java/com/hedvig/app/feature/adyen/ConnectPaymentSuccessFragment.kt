package com.hedvig.app.feature.adyen

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.ConnectPaymentResultFragmentBinding
import com.hedvig.app.feature.trustly.TrustlyTracker
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConnectPaymentSuccessFragment : Fragment(R.layout.connect_payment_result_fragment) {
    private val binding by viewBinding(ConnectPaymentResultFragmentBinding::bind)
    private val connectPaymentViewModel: ConnectPaymentViewModel by sharedViewModel()
    private val tracker: TrustlyTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            val success = requireArguments().getBoolean(SUCCESS)
            val isPostSign = requireArguments().getBoolean(IS_POST_SIGN)

            if (success) {
                resultIcon.setImageResource(R.drawable.ic_active)
                resultTitle.setText(R.string.PROFILE_TRUSTLY_SUCCESS_TITLE)
                resultDoItLater.isVisible = false
                resultClose.setText(
                    if (isPostSign) {
                        R.string.ONBOARDING_CONNECT_DD_SUCCESS_CTA
                    } else {
                        R.string.PROFILE_TRUSTLY_CLOSE
                    }
                )
                resultClose.setHapticClickListener {
                    tracker.close()
                    connectPaymentViewModel.close()
                }
            } else {
                resultIcon.setImageResource(R.drawable.ic_terminated)
                resultTitle.setText(R.string.ONBOARDING_CONNECT_DD_FAILURE_HEADLINE)
                resultDoItLater.isVisible = true
                resultDoItLater.setHapticClickListener {
                    tracker.doItLater()
                    connectPaymentViewModel.close()
                }
                resultClose.setText(R.string.ONBOARDING_CONNECT_DD_FAILURE_CTA_RETRY)
                resultClose.setHapticClickListener {
                    tracker.retry()
                    connectPaymentViewModel.navigateTo(ConnectPaymentScreenState.Connect)
                }
            }
        }
    }

    companion object {
        private const val SUCCESS = "SUCCESS"
        private const val IS_POST_SIGN = "IS_POST_SIGN"

        fun newInstance(success: Boolean, isPostSign: Boolean) =
            ConnectPaymentSuccessFragment().apply {
                arguments = bundleOf(
                    SUCCESS to success,
                    IS_POST_SIGN to isPostSign
                )
            }
    }
}
