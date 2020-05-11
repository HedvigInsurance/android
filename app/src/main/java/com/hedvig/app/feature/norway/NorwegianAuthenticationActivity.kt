package com.hedvig.app.feature.norway

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.extensions.observe
import e
import kotlinx.android.synthetic.main.activity_norwegian_authentication.*
import org.koin.android.viewmodel.ext.android.viewModel

class NorwegianAuthenticationActivity : BaseActivity(R.layout.activity_norwegian_authentication) {
    private val model: NorwegianAuthenticationViewModel by viewModel()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hedvigToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        norwegianBankIdContainer.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        norwegianBankIdContainer.webViewClient = object : WebViewClient() {
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

        model.authStatus.observe(this) { status ->
            status?.let { s ->
                when (s) {
                    AuthState.SUCCESS -> {
                        startActivity(
                            LoggedInActivity.newInstance(
                                this@NorwegianAuthenticationActivity,
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
        }


        model.redirectUrl.observe(this) { redirectUrl ->
            redirectUrl?.let { ru ->
                norwegianBankIdContainer.loadUrl(ru)
            }
        }
    }

    companion object {
        fun newInstance(context: Context) =
            Intent(context, NorwegianAuthenticationActivity::class.java)
    }
}
