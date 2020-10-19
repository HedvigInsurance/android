package com.hedvig.app.feature.connectpayin

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
import e
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConnectPaymentResultFragment : Fragment(R.layout.connect_payment_result_fragment) {
    private val binding by viewBinding(ConnectPaymentResultFragmentBinding::bind)
    private val connectPaymentViewModel: ConnectPaymentViewModel by sharedViewModel()
    private val tracker: TrustlyTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            val success = requireArguments().getBoolean(SUCCESS)

            val payinType = requireArguments().getSerializable(PAYIN_TYPE) as? ConnectPayinType

            if (payinType == null) {
                e { "Programmer error: PAYIN_TYPE not supplied to ${this.javaClass.name}" }
                return
            }

            if (success) {
                icon.setImageResource(R.drawable.ic_checkmark_in_circle)
                title.setText(
                    when (payinType) {
                        ConnectPayinType.ADYEN -> R.string.pay_in_confirmation_headline
                        ConnectPayinType.TRUSTLY -> R.string.pay_in_confirmation_direct_debit_headline
                    }
                )
                doItLater.isVisible = false
                close.setText(R.string.pay_in_confirmation_continue_button)
                close.setHapticClickListener {
                    tracker.close()
                    connectPaymentViewModel.close()
                }
            } else {
                icon.setImageResource(R.drawable.ic_warning_triangle)
                title.setText(R.string.pay_in_error_headline)
                body.setText(
                    when (payinType) {
                        ConnectPayinType.TRUSTLY -> R.string.pay_in_error_direct_debit_body
                        ConnectPayinType.ADYEN -> R.string.pay_in_error_body
                    }
                )
                body.isVisible = true
                doItLater.isVisible = true
                doItLater.setHapticClickListener {
                    tracker.doItLater()
                    connectPaymentViewModel.close()
                }
                close.setText(R.string.pay_in_error_retry_button)
                close.setHapticClickListener {
                    tracker.retry()
                    connectPaymentViewModel.navigateTo(
                        ConnectPaymentScreenState.Connect(
                            TransitionType.ENTER_RIGHT_EXIT_RIGHT
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val SUCCESS = "SUCCESS"
        private const val PAYIN_TYPE = "PAYIN_TYPE"

        fun newInstance(success: Boolean, payinType: ConnectPayinType) =
            ConnectPaymentResultFragment().apply {
                arguments = bundleOf(
                    SUCCESS to success,
                    PAYIN_TYPE to payinType
                )
            }
    }
}
