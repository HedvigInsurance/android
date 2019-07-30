package com.hedvig.app.feature.profile.ui.payment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.findNavController
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.viewmodel.DirectDebitViewModel
import kotlinx.android.synthetic.main.fragment_trustly.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class TrustlyFragment : androidx.fragment.app.Fragment() {

    val profileViewModel: ProfileViewModel by sharedViewModel()
    val directDebitViewModel: DirectDebitViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_trustly, container, false)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trustlyContainer.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        loadUrl()
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
        profileViewModel.startTrustlySession()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (trustlyContainer.parent as ViewGroup).removeView(trustlyContainer)

        trustlyContainer.removeAllViews()
        trustlyContainer.destroy()
    }

    private fun goBack() {
        this.view?.findNavController()?.popBackStack()
    }

    fun showSuccess() {
        trustlyContainer.remove()
        resultIcon.setImageResource(R.drawable.icon_success)
        resultTitle.text = resources.getString(R.string.PROFILE_TRUSTLY_SUCCESS_TITLE)
        resultParagraph.text = resources.getString(R.string.PROFILE_TRUSTLY_SUCCESS_DESCRIPTION)
        resultClose.background.compatSetTint(requireContext().compatColor(R.color.green))
        resultClose.setOnClickListener {
            profileViewModel.refreshBankAccountInfo()
            directDebitViewModel.refreshDirectDebitStatus()
            goBack()
        }
        resultScreen.show()
    }

    fun showFailure() {
        trustlyContainer.remove()
        resultIcon.setImageResource(R.drawable.icon_failure)
        resultTitle.text = resources.getString(R.string.PROFILE_TRUSTLY_FAILURE_TITLE)
        resultParagraph.text = resources.getString(R.string.PROFILE_TRUSTLY_FAILURE_DESCRIPTION)
        resultClose.background.compatSetTint(requireContext().compatColor(R.color.pink))
        resultClose.setOnClickListener {
            goBack()
        }
        resultScreen.show()
    }
}
