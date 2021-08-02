package com.hedvig.app.feature.zignsec

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.SimpleSignAuthenticationActivityBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.zignsec.ui.ErrorFragment
import com.hedvig.app.feature.zignsec.ui.IdentityInputFragment
import com.hedvig.app.feature.zignsec.ui.ZignSecWebViewFragment
import com.hedvig.app.util.extensions.addToBackStack
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SimpleSignAuthenticationActivity : BaseActivity(R.layout.simple_sign_authentication_activity) {
    private val binding by viewBinding(SimpleSignAuthenticationActivityBinding::bind)
    private val model: SimpleSignAuthenticationViewModel by viewModel { parametersOf(data) }

    private val data by lazy {
        intent.getParcelableExtra<SimpleSignAuthenticationData>(DATA)
            ?: throw Error("Programmer error: DATA not passed to ${this.javaClass.name}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.toolbar.apply {
            applyStatusBarInsets()
            setNavigationOnClickListener { finish() }
        }
        binding.container.applyNavigationBarInsets()
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.container, IdentityInputFragment.newInstance(data))
            }
        }

        model.authStatus.observe(this) {
            when (it) {
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
        supportFragmentManager.popBackStack(
            supportFragmentManager.getBackStackEntryAt(0).id,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
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
