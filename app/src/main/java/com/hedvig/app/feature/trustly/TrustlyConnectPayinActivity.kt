package com.hedvig.app.feature.trustly

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.StartDirectDebitRegistrationMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.TrustlyConnectFragmentBinding
import com.hedvig.app.feature.adyen.ConnectPaymentScreenState
import com.hedvig.app.feature.adyen.ConnectPaymentSuccessFragment
import com.hedvig.app.feature.adyen.ConnectPaymentViewModel
import com.hedvig.app.feature.adyen.PostSignExplainerFragment
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class TrustlyConnectPayinActivity : BaseActivity(R.layout.fragment_container_activity) {
    private val connectPaymentViewModel: ConnectPaymentViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isPostSign()) {
            connectPaymentViewModel.setInitialNavigationDestination(ConnectPaymentScreenState.Explainer)
            connectPaymentViewModel.isReadyToStart()
        } else {
            connectPaymentViewModel.navigateTo(ConnectPaymentScreenState.Connect)
        }

        connectPaymentViewModel.navigationState.observe(this) { state ->
            when (state) {
                ConnectPaymentScreenState.Explainer -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, PostSignExplainerFragment())
                    .commitAllowingStateLoss()
                ConnectPaymentScreenState.Connect -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, TrustlyConnectFragment())
                    .commitAllowingStateLoss()
                is ConnectPaymentScreenState.Result -> supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.container,
                        ConnectPaymentSuccessFragment.newInstance(state.success, isPostSign())
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

class TrustlyConnectFragment : Fragment(R.layout.trustly_connect_fragment) {
    private val binding by viewBinding(TrustlyConnectFragmentBinding::bind)
    private val trustlyViewModel: TrustlyViewModel by viewModel()
    private val connectPaymentViewModel: ConnectPaymentViewModel by sharedViewModel()

    private lateinit var trustlyUrl: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.trustly.apply {
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
                // TODO: Add a loading state here so that we indicate progress to the user
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
                        connectPaymentViewModel.navigateTo(ConnectPaymentScreenState.Result(false))
                        return
                    }
                }
            }
            trustlyViewModel.data.observe(viewLifecycleOwner) { url ->
                trustlyUrl = url
                loadUrl(url)
            }
        }
    }
}

abstract class TrustlyViewModel : ViewModel() {
    protected val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data
}

class TrustlyViewModelImpl(
    private val repository: TrustlyRepository
) : TrustlyViewModel() {
    init {
        viewModelScope.launch {
            val response = runCatching { repository.startTrustlySessionAsync().await() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.startDirectDebitRegistration?.let { _data.postValue(it) }
        }
    }
}

class TrustlyRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun startTrustlySessionAsync() = apolloClientWrapper
        .apolloClient
        .mutate(StartDirectDebitRegistrationMutation())
        .toDeferred()
}
