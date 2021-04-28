package com.hedvig.app.feature.zignsec.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.HttpAuthHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityZignSecAuthenticationBinding
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.hedvig.app.util.extensions.viewBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ZignSecWebViewFragment : Fragment(R.layout.activity_zign_sec_authentication) {
    private val binding by viewBinding(ActivityZignSecAuthenticationBinding::bind)
    private val model: SimpleSignAuthenticationViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    }

    @SuppressLint("SetJavaScriptEnabled") // JavaScript is required for ZignSec to function.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            danishBankIdContainer.apply {
                settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        if (request?.url?.toString()?.contains("success") == true) {
                            return true
                        }
                        if (request?.url?.toString()?.contains("fail") == true) {
                            model.authFailed()
                            return true
                        }
                        request?.url?.toString()?.let { view?.loadUrl(it) }
                        return true
                    }

                    override fun onReceivedHttpAuthRequest(
                        view: WebView?,
                        handler: HttpAuthHandler?,
                        host: String?,
                        realm: String?,
                    ) {
                        handler?.proceed("hedvig", "hedvig1234")
                    }
                }
            }
            model.zignSecUrl.observe(viewLifecycleOwner) { danishBankIdContainer.loadUrl(it) }
        }
    }

    companion object {
        fun newInstance() = ZignSecWebViewFragment()
    }
}
