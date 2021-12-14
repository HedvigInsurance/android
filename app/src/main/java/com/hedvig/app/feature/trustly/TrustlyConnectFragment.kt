package com.hedvig.app.feature.trustly

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.TrustlyConnectFragmentBinding
import com.hedvig.app.feature.connectpayin.ConnectPayinType
import com.hedvig.app.feature.connectpayin.ConnectPaymentScreenState
import com.hedvig.app.feature.connectpayin.ConnectPaymentViewModel
import com.hedvig.app.feature.connectpayin.TransitionType
import com.hedvig.app.feature.connectpayin.showConfirmCloseDialog
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.onBackPressedCallback
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrustlyConnectFragment : Fragment(R.layout.trustly_connect_fragment) {
    private var binding: TrustlyConnectFragmentBinding? = null
    private val trustlyViewModel: TrustlyViewModel by viewModel()
    private val connectPaymentViewModel: ConnectPaymentViewModel by sharedViewModel()
    private val trackingFacade: TrackingFacade by inject()

    private var hasLoadedWebView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transitionType =
            requireArguments().getSerializable(TRANSITION_TYPE) as? TransitionType ?: return

        if (transitionType != TransitionType.NO_ENTER_EXIT_RIGHT) {
            enterTransition = MaterialSharedAxis(
                MaterialSharedAxis.X,
                transitionType == TransitionType.ENTER_LEFT_EXIT_RIGHT
            )
        }
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = TrustlyConnectFragmentBinding.bind(view)

        val isPostSign = requireArguments().getBoolean(IS_POST_SIGN)

        if (isPostSign) {
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                onBackPressedCallback({
                    showConfirmCloseDialog(
                        requireContext(),
                        ConnectPayinType.TRUSTLY,
                        connectPaymentViewModel::close
                    )
                })
            )
        }

        binding?.toolbar?.apply {
            if (isPostSign) {
                inflateMenu(R.menu.connect_payin)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.skip -> {
                            showConfirmCloseDialog(
                                requireContext(),
                                ConnectPayinType.TRUSTLY,
                                connectPaymentViewModel::close
                            )
                            true
                        }
                        else -> false
                    }
                }
            } else {
                setNavigationIcon(R.drawable.ic_close)
                setNavigationOnClickListener {
                    connectPaymentViewModel.close()
                }
            }
        }
        binding?.trustly?.apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                setSupportMultipleWindows(true)
            }
            webChromeClient = TrustlyWebChromeClient()
            addJavascriptInterface(
                TrustlyJavascriptInterface(requireActivity()),
                TrustlyJavascriptInterface.NAME
            )
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding?.apply {
                        if (!hasLoadedWebView) {
                            TransitionManager.beginDelayedTransition(
                                root,
                                MaterialFadeThrough()
                            )
                            loadingContainer.isVisible = false
                            trustly.isVisible = true
                            hasLoadedWebView = true
                            trackingFacade.track("connect_payment_visible")
                        }
                    }
                }

                override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
                    if (url.startsWith("bankid")) {
                        view?.stopLoading()
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                        return
                    }

                    if (url.contains("success")) {
                        view?.stopLoading()
                        connectPaymentViewModel.navigateTo(ConnectPaymentScreenState.Result(true))
                        return
                    }
                    if (url.contains("fail")) {
                        view?.stopLoading()
                        connectPaymentViewModel.navigateTo(
                            ConnectPaymentScreenState.Result(
                                false
                            )
                        )
                        return
                    }
                }
            }
        }
        trustlyViewModel.data.observe(viewLifecycleOwner) { url ->
            binding?.trustly?.loadUrl(url)
        }
    }

    override fun onDestroyView() {
        destroyWebView()
        binding = null
        super.onDestroyView()
    }

    private fun destroyWebView() = binding?.apply {
        trustly.removeAllViews()
        trustly.clearHistory()
        trustly.clearCache(true)
        trustly.destroy()
    }

    companion object {
        private const val IS_POST_SIGN = "IS_POST_SIGN"
        private const val TRANSITION_TYPE = "TRANSITION_TYPE"

        fun newInstance(isPostSign: Boolean, transitionType: TransitionType) =
            TrustlyConnectFragment().apply {
                arguments = bundleOf(
                    IS_POST_SIGN to isPostSign,
                    TRANSITION_TYPE to transitionType
                )
            }
    }
}
