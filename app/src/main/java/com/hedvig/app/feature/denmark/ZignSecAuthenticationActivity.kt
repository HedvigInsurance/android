package com.hedvig.app.feature.denmark

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityZignSecAuthenticationBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.extensions.viewBinding
import e
import org.koin.android.viewmodel.ext.android.viewModel

class ZignSecAuthenticationActivity : AppCompatActivity(R.layout.activity_zign_sec_authentication) {

    private val viewModel: ZignSecAuthViewModel by viewModel()
    private val binding by viewBinding(ActivityZignSecAuthenticationBinding::bind)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            danishBankIdContainer.apply {
                settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        if (request?.url?.toString()?.contains("success") == true) {
                            return true
                        }
                        if (request?.url?.toString()?.contains("failure") == true) {
                            // TODO: Add UI for the failure case
                            e { "Failed to log in" }
                            return true
                        }
                        view?.loadUrl(request?.url?.toString())
                        return true
                    }
                }
            }
        }

        viewModel.authStatus.observe(this) { status ->
            when (status) {
                AuthState.SUCCESS -> {
                    startActivity(
                        LoggedInActivity.newInstance(
                            this,
                            withoutHistory = true
                        )
                    )
                }
                AuthState.FAILED -> {
                    // TODO: Add UI for the failure case
                    e { "Failed to log in" }
                }
                else -> {
                    // No-op
                }
            }
        }

        viewModel.redirectUrl.observe(this) { url ->
            binding.danishBankIdContainer.loadUrl(url)
        }
    }

    companion object {
        fun newInstance(context: Context) =
            Intent(context, ZignSecAuthenticationActivity::class.java)
    }
}
