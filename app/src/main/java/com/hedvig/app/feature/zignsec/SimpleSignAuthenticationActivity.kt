package com.hedvig.app.feature.zignsec

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.webkit.HttpAuthHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toFlow
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.DanishAuthMutation
import com.hedvig.android.owldroid.graphql.NorwegianBankIdAuthMutation
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityZignSecAuthenticationBinding
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.databinding.IdentityInputFragmentBinding
import com.hedvig.app.databinding.SimpleSignAuthenticationActivityBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.extensions.onImeAction
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_zign_sec_authentication.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

sealed class SimpleSignStartAuthResult {
    data class Success(val url: String) :
        SimpleSignStartAuthResult()

    object Error : SimpleSignStartAuthResult()
}

class StartDanishAuthUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(personalIdentificationNumber: String) =
        when (val response = apolloClient.mutate(DanishAuthMutation(personalIdentificationNumber)).safeQuery()) {
            is QueryResult.Error -> SimpleSignStartAuthResult.Error
            is QueryResult.Success -> {
                val redirectUrl = response.data.danishBankIdAuth.redirectUrl
                SimpleSignStartAuthResult.Success(redirectUrl)
            }
        }
}

class StartNorwegianAuthUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(nationalIdentityNumber: String) =
        when (val response = apolloClient.mutate(NorwegianBankIdAuthMutation(nationalIdentityNumber)).safeQuery()) {
            is QueryResult.Error -> SimpleSignStartAuthResult.Error
            is QueryResult.Success -> {
                val redirectUrl = response.data.norwegianBankIdAuth.redirectUrl
                SimpleSignStartAuthResult.Success(redirectUrl)
            }
        }
}

class SubscribeToAuthStatusUseCase(
    private val apolloClient: ApolloClient,
) {
    operator fun invoke() = apolloClient.subscribe(AuthStatusSubscription()).toFlow()
}

class SimpleSignAuthenticationViewModel(
    private val data: SimpleSignAuthenticationData,
    private val startDanishAuthUseCase: StartDanishAuthUseCase,
    private val startNorwegianAuthUseCase: StartNorwegianAuthUseCase,
    private val subscribeToAuthStatusUseCase: SubscribeToAuthStatusUseCase,
) : ViewModel() {
    private val _input = MutableLiveData("")
    val input: LiveData<String> = _input
    val isValid = input.map {
        when (data.market) {
            Market.NO -> NORWEGIAN_NATIONAL_IDENTITY_NUMBER.matches(it)
            Market.DK -> DANISH_PERSONAL_IDENTIFICATION_NUMBER.matches(it)
            else -> false
        }
    }

    private val _isSubmitting = MutableLiveData(false)
    val isSubmitting: LiveData<Boolean> = _isSubmitting

    private val _zignSecUrl = MutableLiveData<String>()
    val zignSecUrl: LiveData<String> = _zignSecUrl

    private val _authStatus = MutableLiveData<AuthState>()
    val authStatus: LiveData<AuthState> = _authStatus

    private val _events = LiveEvent<Event>()
    val events: LiveData<Event> = _events

    sealed class Event {
        object Success : Event()
        object Error : Event()
        object LoadWebView : Event()
        object Restart : Event()
    }

    init {
        viewModelScope.launch {
            subscribeToAuthStatusUseCase().onEach { response ->
                when (response.data?.authStatus?.status) {
                    AuthState.SUCCESS -> {
                        _events.postValue(Event.Success)
                    }
                    AuthState.FAILED -> {
                        _events.postValue(Event.Error)
                    }
                    else -> {
                    }
                }
            }
                .catch { ex ->
                    e(ex)
                    _events.postValue(Event.Error)
                }
                .launchIn(this)
        }
    }

    fun setInput(text: CharSequence?) {
        text?.toString()?.let { _input.value = it }
    }

    fun authFailed() {
        _events.value = Event.Error
    }

    fun startZignSec() {
        if (isSubmitting.value == true) {
            return
        }
        _isSubmitting.value = true
        when (data.market) {
            Market.NO -> {
                val nationalIdentityNumber = input.value ?: return
                viewModelScope.launch {
                    handleStartAuth(startNorwegianAuthUseCase(nationalIdentityNumber))
                }
            }
            Market.DK -> {
                val personalIdentificationNumber = input.value ?: return
                viewModelScope.launch {
                    handleStartAuth(startDanishAuthUseCase(personalIdentificationNumber))
                }
            }
            else -> {
            }
        }
    }

    private fun handleStartAuth(result: SimpleSignStartAuthResult) {
        when (result) {
            is SimpleSignStartAuthResult.Success -> {
                _zignSecUrl.postValue(result.url)
                _events.postValue(Event.LoadWebView)
            }
            SimpleSignStartAuthResult.Error -> {
                _events.postValue(Event.Error)
            }
        }
        _isSubmitting.postValue(false)
    }

    fun restart() {
        _events.value = Event.Restart
    }

    companion object {
        private val DANISH_PERSONAL_IDENTIFICATION_NUMBER = Regex("[0-9]{10}")
        private val NORWEGIAN_NATIONAL_IDENTITY_NUMBER = Regex("[0-9]{11}")
    }
}

class SimpleSignAuthenticationActivity : BaseActivity(R.layout.simple_sign_authentication_activity) {
    private val binding by viewBinding(SimpleSignAuthenticationActivityBinding::bind)
    private val model: SimpleSignAuthenticationViewModel by viewModel { parametersOf(data) }

    private val data by lazy {
        intent.getParcelableExtra<SimpleSignAuthenticationData>(DATA)
            ?: throw Error("Programmer error: DATA not passed to ${this.javaClass.name}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { finish() }
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.container, IdentityInputFragment.newInstance(data))
            }
        }

        model.authStatus.observe(this) {
            when (it) {
                AuthState.SUCCESS -> {
                }
                AuthState.FAILED -> {
                    showError()
                }
                else -> {
                }
            }
        }
        model.events.observe(this) {
            when (it) {
                SimpleSignAuthenticationViewModel.Event.LoadWebView -> showWebView()
                SimpleSignAuthenticationViewModel.Event.Success -> goToLoggedIn()
                SimpleSignAuthenticationViewModel.Event.Error -> showError()
                SimpleSignAuthenticationViewModel.Event.Restart -> restart()
            }
        }
    }

    private fun goToLoggedIn() {
        startActivity(
            LoggedInActivity.newInstance(
                this,
                withoutHistory = true
            )
        )
    }

    private fun restart() {
        supportFragmentManager.popBackStack(supportFragmentManager.getBackStackEntryAt(0).id,
            FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun showWebView() {
        supportFragmentManager.commit {
            replace(R.id.container, ZignSecWebViewFragment.newInstance())
            addToBackStack()
        }
    }

    private fun showError() {
        supportFragmentManager.commit {
            replace(R.id.container, ErrorFragment.newInstance())
            addToBackStack()
        }
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(context: Context, market: Market) =
            Intent(context, SimpleSignAuthenticationActivity::class.java).apply {
                putExtra(DATA, SimpleSignAuthenticationData(market))
            }
    }
}

class IdentityInputFragment : Fragment(R.layout.identity_input_fragment) {
    private val binding by viewBinding(IdentityInputFragmentBinding::bind)
    private val model: SimpleSignAuthenticationViewModel by sharedViewModel { parametersOf(data) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    private val data by lazy {
        requireArguments().getParcelable<SimpleSignAuthenticationData>(DATA)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {


            when (data?.market) {
                Market.NO -> {
                    input.setHint(R.string.simple_sign_login_text_field_label)
                    input.setHelperText(R.string.simple_sign_login_text_field_helper_text)
                }
                Market.DK -> {
                    input.setHint(R.string.simple_sign_login_text_field_label_dk)
                    input.setHelperText(R.string.simple_sign_login_text_field_helper_text_dk)
                }
                else -> {
                }
            }

            inputText.apply {
                doOnTextChanged { text, _, _, _ -> model.setInput(text) }
                onImeAction { startZignSecIfValid() }
            }
            model.isValid.observe(viewLifecycleOwner) {
                if (model.isSubmitting.value != true) {
                    signIn.isEnabled = it
                }
            }
            model.isSubmitting.observe(viewLifecycleOwner) { isSubmitting ->
                signIn.isEnabled = !isSubmitting
            }

            signIn.setHapticClickListener {
                startZignSecIfValid()
            }
        }
    }

    private fun startZignSecIfValid() {
        if (model.isValid.value == true) {
            model.startZignSec()
        }
    }

    companion object {

        private const val DATA = "DATA"
        fun newInstance(data: SimpleSignAuthenticationData) = IdentityInputFragment().apply {
            arguments = bundleOf(DATA to data)
        }
    }
}

fun FragmentTransaction.addToBackStack() = addToBackStack(null)

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
                        if (request?.url?.toString()?.contains("failure") == true) {
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
        }
        model.zignSecUrl.observe(viewLifecycleOwner) { danishBankIdContainer.loadUrl(it) }
    }

    companion object {
        fun newInstance() = ZignSecWebViewFragment()
    }
}

class ErrorFragment : Fragment(R.layout.generic_error) {
    private val binding by viewBinding(GenericErrorBinding::bind)
    private val model: SimpleSignAuthenticationViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.retry.setHapticClickListener {
            model.restart()
        }
    }

    companion object {
        fun newInstance() = ErrorFragment()
    }
}

@Parcelize
data class SimpleSignAuthenticationData(
    val market: Market,
) : Parcelable
