package com.hedvig.app.feature.profile.ui.payment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.fadeOut
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.viewmodel.DirectDebitViewModel
import kotlinx.android.synthetic.main.activity_trustly.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class TrustlyActivity : BaseActivity() {

    private val profileViewModel: ProfileViewModel by viewModel()
    private val directDebitViewModel: DirectDebitViewModel by viewModel()

    private val tracker: TrustlyTracker by inject()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trustly)

        trustlyContainer.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        notNow.setHapticClickListener {
            tracker.notNow()
            showConfirmCloseDialog()
        }

        if (isPostSignDD()) {
            loadingSpinner.remove()
            explainerScreen.show()
            explainerButton.setHapticClickListener {
                tracker.explainerConnect()
                explainerScreen.fadeOut({
                    toolbar.show()
                    loadingSpinner.show()
                    profileViewModel.startTrustlySession()
                }, true)
            }
        }
        loadUrl()
    }

    override fun onDestroy() {
        (trustlyContainer.parent as ViewGroup).removeView(trustlyContainer)

        trustlyContainer.removeAllViews()
        trustlyContainer.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (isPostSignDD()) {
            showConfirmCloseDialog()
            return
        }
        close()
    }

    private fun showConfirmCloseDialog() {
        showAlert(
            title = R.string.TRUSTLY_ALERT_TITLE,
            message = R.string.TRUSTLY_ALERT_BODY,
            positiveLabel = R.string.TRUSTLY_ALERT_POSITIVE_ACTION,
            negativeLabel = R.string.TRUSTLY_ALERT_NEGATIVE_ACTION,
            positiveAction = {
                close()
            }
        )
    }

    private fun loadUrl() {
        profileViewModel.trustlyUrl.observe(lifecycleOwner = this) { url ->
            trustlyContainer.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                    super.onPageFinished(view, url)
                    if (loadedUrl != url) {
                        return
                    }

                    loadingSpinner.remove()
                    trustlyContainer.fadeIn()
                }

                override fun onPageStarted(view: WebView?, requestedUrl: String, favicon: Bitmap?) {
                    if (requestedUrl.startsWith("bankid")) {
                        view?.stopLoading()
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(requestedUrl)
                        startActivity(intent)
                        return
                    }

                    if (requestedUrl.contains("success")) {
                        view?.stopLoading()
                        showSuccess()
                        return
                    }
                    if (requestedUrl.contains("fail")) {
                        view?.stopLoading()
                        showFailure()
                        return
                    }
                }
            }
            trustlyContainer.loadUrl(url)
        }
        if (isPostSignDD()) {
            return
        }
        profileViewModel.startTrustlySession()
        toolbar.show()
    }

    fun showSuccess() {
        tracker.addPaymentInfo()
        trustlyContainer.remove()
        resultIcon.setImageResource(R.drawable.icon_success)
        resultTitle.text = resources.getString(R.string.PROFILE_TRUSTLY_SUCCESS_TITLE)
        if (isPostSignDD()) {
            resultParagraph.text = getString(R.string.ONBOARDING_CONNECT_DD_SUCCESS_CTA)
        } else {
            resultParagraph.text = getString(R.string.PROFILE_TRUSTLY_SUCCESS_DESCRIPTION)
        }
        resultClose.setHapticClickListener {
            profileViewModel.refreshBankAccountInfo()
            directDebitViewModel.refreshDirectDebitStatus()
            close()
        }
        resultScreen.show()
    }

    fun showFailure() {
        trustlyContainer.remove()
        resultIcon.setImageResource(R.drawable.icon_failure)
        resultTitle.text = resources.getString(R.string.ONBOARDING_CONNECT_DD_FAILURE_HEADLINE)
        resultParagraph.text = getString(R.string.ONBOARDING_CONNECT_DD_FAILURE_BODY)
        resultDoItLater.show()
        resultDoItLater.setHapticClickListener {
            tracker.doItLater()
            close()
        }
        resultClose.text = getString(R.string.ONBOARDING_CONNECT_DD_FAILURE_CTA_RETRY)
        resultClose.setHapticClickListener {
            tracker.retry()
            loadingSpinner.show()
            resultScreen.remove()
            profileViewModel.startTrustlySession()
        }
        resultScreen.show()
    }

    private fun close() {
        if (isPostSignDD()) {
            startActivity(Intent(this, LoggedInActivity::class.java).apply {
                putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            return
        }
        super.onBackPressed()
    }

    private fun isPostSignDD() = intent.getBooleanExtra(WITH_EXPLAINER, false)

    companion object {
        private const val WITH_EXPLAINER = "with_explainer"

        fun newInstance(context: Context, withExplainer: Boolean = false) =
            Intent(context, TrustlyActivity::class.java).apply {
                putExtra(WITH_EXPLAINER, withExplainer)
            }
    }
}
