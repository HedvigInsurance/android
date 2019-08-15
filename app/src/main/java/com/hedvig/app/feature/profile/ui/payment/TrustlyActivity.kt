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
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.observe
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

        if (intent.getBooleanExtra(WITH_EXPLAINER, false)) {
            loadingSpinner.remove()
            explainerScreen.show()
            explainerScreen.setHapticClickListener {
                tracker.explainerConnect()
                explainerScreen.remove()
                loadingSpinner.show()
                profileViewModel.startTrustlySession()
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

    private fun loadUrl() {
        profileViewModel.trustlyUrl.observe(this) { url ->
            trustlyContainer.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                    super.onPageFinished(view, url)
                    if (loadedUrl != url) {
                        return
                    }

                    loadingSpinner.remove()
                    trustlyContainer.show()
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
        if (!intent.getBooleanExtra(WITH_EXPLAINER, false)) {
            return
        }
        profileViewModel.startTrustlySession()
    }

    fun showSuccess() {
        tracker.addPaymentInfo()
        trustlyContainer.remove()
        resultIcon.setImageResource(R.drawable.icon_success)
        resultTitle.text = resources.getString(R.string.PROFILE_TRUSTLY_SUCCESS_TITLE)
        resultParagraph.text = resources.getString(R.string.PROFILE_TRUSTLY_SUCCESS_DESCRIPTION)
        resultClose.background.compatSetTint(compatColor(R.color.green))
        resultClose.setHapticClickListener {
            profileViewModel.refreshBankAccountInfo()
            directDebitViewModel.refreshDirectDebitStatus()
            if (intent.getBooleanExtra(WITH_EXPLAINER, false)) {
                startActivity(Intent(this, LoggedInActivity::class.java).apply {
                    putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
                return@setHapticClickListener
            }
            onBackPressed()
        }
        resultScreen.show()
    }

    fun showFailure() {
        trustlyContainer.remove()
        resultIcon.setImageResource(R.drawable.icon_failure)
        resultTitle.text = resources.getString(R.string.PROFILE_TRUSTLY_FAILURE_TITLE)
        resultParagraph.text = resources.getString(R.string.PROFILE_TRUSTLY_FAILURE_DESCRIPTION)
        resultClose.background.compatSetTint(compatColor(R.color.pink))
        resultDoItLater.show()
        resultDoItLater.setHapticClickListener {
            if (intent.getBooleanExtra(WITH_EXPLAINER, false)) {
                startActivity(Intent(this, LoggedInActivity::class.java).apply {
                    putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
                return@setHapticClickListener
            }
            onBackPressed()
        }
        resultClose.setHapticClickListener {
            loadingSpinner.show()
            resultScreen.remove()
            profileViewModel.startTrustlySession()
        }
        resultScreen.show()
    }

    companion object {
        private const val WITH_EXPLAINER = "with_explainer"

        fun newInstance(context: Context, withExplainer: Boolean = false) =
            Intent(context, TrustlyActivity::class.java).apply {
                putExtra(WITH_EXPLAINER, withExplainer)
            }
    }
}
