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
import com.hedvig.android.auth.FAILURE_CALLBACK_URL
import com.hedvig.android.auth.SUCCESS_CALLBACK_URL
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityZignSecAuthenticationBinding
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ZignSecWebViewFragment : Fragment(R.layout.activity_zign_sec_authentication) {
  private val binding by viewBinding(ActivityZignSecAuthenticationBinding::bind)
  private val viewModel: SimpleSignAuthenticationViewModel by activityViewModel()

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
            if (request?.url?.toString()?.contains(SUCCESS_CALLBACK_URL) == true) {
              return true
            }
            if (request?.url?.toString()?.contains(FAILURE_CALLBACK_URL) == true) {
              viewModel.authFailed()
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
      viewModel.zignSecUrl.observe(viewLifecycleOwner) { danishBankIdContainer.loadUrl(it) }
    }
  }

  companion object {
    fun newInstance() = ZignSecWebViewFragment()
  }
}
