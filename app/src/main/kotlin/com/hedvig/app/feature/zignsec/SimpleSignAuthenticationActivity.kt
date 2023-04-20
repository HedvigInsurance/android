package com.hedvig.app.feature.zignsec

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.market.Market
import com.hedvig.app.R
import com.hedvig.app.databinding.SimpleSignAuthenticationActivityBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.zignsec.ui.ErrorFragment
import com.hedvig.app.feature.zignsec.ui.IdentityInputFragment
import com.hedvig.app.feature.zignsec.ui.ZignSecWebViewFragment
import com.hedvig.app.util.extensions.addToBackStack
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import slimber.log.d

class SimpleSignAuthenticationActivity : AppCompatActivity(R.layout.simple_sign_authentication_activity) {
  private val binding by viewBinding(SimpleSignAuthenticationActivityBinding::bind)
  private val viewModel: SimpleSignAuthenticationViewModel by viewModel { parametersOf(data) }

  private val data by lazy {
    intent.parcelableExtra<SimpleSignAuthenticationData>(DATA)
      ?: error("Programmer error: DATA not passed to ${this.javaClass.name}")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.compatSetDecorFitsSystemWindows(false)
    onBackPressedDispatcher.addCallback(this) {
      d { "SimpleSignAuthenticationActivity: invoked back. Going back to marketing" }
      remove()
      onBackPressedDispatcher.onBackPressed()
    }
    binding.toolbar.apply {
      applyStatusBarInsets()
      setNavigationOnClickListener {
        onBackPressedDispatcher.onBackPressed()
      }
    }
    binding.container.applyNavigationBarInsets()
    if (savedInstanceState == null) {
      supportFragmentManager.commit {
        replace(R.id.container, IdentityInputFragment.newInstance(data))
      }
    }

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.subscribeToAuthSuccessEvent()
      }
    }

    viewModel.events.observe(this) { event ->
      d { "Simple sign event:$event" }
      when (event) {
        SimpleSignAuthenticationViewModel.Event.LoadWebView -> showWebView()
        SimpleSignAuthenticationViewModel.Event.Success -> goToLoggedIn()
        SimpleSignAuthenticationViewModel.Event.Error -> showError()
        SimpleSignAuthenticationViewModel.Event.CancelSignIn -> finish()
      }
    }
  }

  private fun goToLoggedIn() {
    startActivity(
      LoggedInActivity.newInstance(
        this,
        withoutHistory = true,
      ),
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
