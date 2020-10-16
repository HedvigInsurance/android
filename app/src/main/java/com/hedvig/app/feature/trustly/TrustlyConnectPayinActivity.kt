package com.hedvig.app.feature.trustly

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.observe
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.connectpayin.ConnectPaymentResultFragment
import com.hedvig.app.feature.connectpayin.ConnectPaymentScreenState
import com.hedvig.app.feature.connectpayin.ConnectPaymentViewModel
import com.hedvig.app.feature.connectpayin.PostSignExplainerFragment
import com.hedvig.app.feature.connectpayin.TransitionType
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.extensions.showAlert
import org.koin.android.viewmodel.ext.android.viewModel

class TrustlyConnectPayinActivity : BaseActivity(R.layout.fragment_container_activity) {
    private val connectPaymentViewModel: ConnectPaymentViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isPostSign()) {
            connectPaymentViewModel.setInitialNavigationDestination(ConnectPaymentScreenState.Explainer)
            connectPaymentViewModel.isReadyToStart()
        } else {
            connectPaymentViewModel.navigateTo(ConnectPaymentScreenState.Connect(TransitionType.NO_ENTER_EXIT_RIGHT))
        }

        connectPaymentViewModel.navigationState.observe(this) { state ->
            when (state) {
                ConnectPaymentScreenState.Explainer -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, PostSignExplainerFragment.newInstance(isPostSign()))
                    .commitAllowingStateLoss()
                is ConnectPaymentScreenState.Connect -> supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.container,
                        TrustlyConnectFragment.newInstance(isPostSign(), state.transitionType)
                    )
                    .commitAllowingStateLoss()
                is ConnectPaymentScreenState.Result -> supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.container,
                        ConnectPaymentResultFragment.newInstance(state.success)
                    )
                    .commitAllowingStateLoss()
            }
        }
        connectPaymentViewModel.shouldClose.observe(this) { shouldClose ->
            if (shouldClose) {
                if (isPostSign()) {
                    startActivity(
                        LoggedInActivity.newInstance(
                            this,
                            withoutHistory = true,
                            isFromOnboarding = true
                        )
                    )
                    return@observe
                }
                finish()
            }
        }
    }

    private fun isPostSign() = intent.getBooleanExtra(IS_POST_SIGN, false)

    companion object {
        private const val IS_POST_SIGN = "IS_POST_SIGN"
        fun newInstance(context: Context, isPostSign: Boolean = false) =
            Intent(context, TrustlyConnectPayinActivity::class.java).apply {
                putExtra(IS_POST_SIGN, isPostSign)
            }
    }
}

inline fun onBackPressedCallback(crossinline callback: () -> Unit, enabled: Boolean = true) =
    object : OnBackPressedCallback(enabled) {
        override fun handleOnBackPressed() {
            callback()
        }
    }

fun showConfirmCloseDialog(context: Context, close: () -> Unit) = context.showAlert(
    title = R.string.pay_in_iframe_post_sign_skip_alert_title,
    message = R.string.pay_in_iframe_post_sign_skip_alert_body,
    positiveLabel = R.string.pay_in_iframe_post_sign_skip_alert_proceed_button,
    negativeLabel = R.string.pay_in_iframe_post_sign_skip_alert_dismiss_button,
    positiveAction = {
        close()
    }
)
