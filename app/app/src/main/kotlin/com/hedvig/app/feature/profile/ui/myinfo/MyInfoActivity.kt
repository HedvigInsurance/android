package com.hedvig.app.feature.profile.ui.myinfo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.validation.validateEmail
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMyInfoBinding
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.validatePhoneNumber
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyInfoActivity : AppCompatActivity(R.layout.activity_my_info) {
  private val viewModel: MyInfoViewModel by viewModel()
  private val binding by viewBinding(ActivityMyInfoBinding::bind)

  private var hasEditedPhoneOrEmail: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    binding.apply {
      window.compatSetDecorFitsSystemWindows(false)
      scrollView.applyNavigationBarInsets()
      setupToolbar(R.id.toolbar, hedvig.resources.R.drawable.ic_back, true) {
        onBackPressedDispatcher.onBackPressed()
      }
      toolbar.title = getString(hedvig.resources.R.string.PROFILE_MY_INFO_TITLE)
    }
    setupEmailInput()
    setupPhoneNumberInput()
    loadData()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.my_info_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    if (!hasEditedPhoneOrEmail) {
      menu.removeItem(R.id.save)
    }
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val prevEmail = viewModel.data.value.member?.email ?: ""
    val prevPhoneNumber = viewModel.data.value.member?.phoneNumber ?: ""

    binding.apply {
      val newEmail = emailInput.text.toString()
      val newPhoneNumber = phoneNumberInput.text.toString()

      if (prevEmail != newEmail && !validateEmail(newEmail).isSuccessful) {
        ValidationDialog.newInstance(
          hedvig.resources.R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_TITLE,
          hedvig.resources.R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_EMAIL,
          hedvig.resources.R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DISMISS,
        ).show(supportFragmentManager, ValidationDialog.TAG)
        return true
      }

      if (prevPhoneNumber != newPhoneNumber && !validatePhoneNumber(newPhoneNumber).isSuccessful) {
        ValidationDialog.newInstance(
          hedvig.resources.R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_TITLE,
          hedvig.resources.R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DESCRIPTION_PHONE_NUMBER,
          hedvig.resources.R.string.PROFILE_MY_INFO_VALIDATION_DIALOG_DISMISS,
        ).show(supportFragmentManager, ValidationDialog.TAG)
        return true
      }

      viewModel.updateEmailAndPhoneNumber()
      hasEditedPhoneOrEmail = false

      if (emailInput.isFocused) {
        emailInput.clearFocus()
      }
      if (phoneNumberInput.isFocused) {
        phoneNumberInput.clearFocus()
      }
      myInfoRoot.dismissKeyboard()
    }
    return true
  }

  private fun loadData() {
    viewModel
      .data
      .flowWithLifecycle(lifecycle)
      .onEach { viewState ->
        binding.apply {
          spinner.loadingSpinner.isVisible = viewState.isLoading
          contactDetailsContainer.isVisible = !viewState.isLoading
          invalidateOptionsMenu()

          if (viewState.member != null && !hasEditedPhoneOrEmail) {
            emailInput.setText(viewState.member.email ?: "")
            phoneNumberInput.setText(viewState.member.phoneNumber ?: "")
          }
        }
      }
      .launchIn(lifecycleScope)
  }

  private fun setupEmailInput() {
    binding.apply {
      emailInput.onChange { value ->
        viewModel.emailChanged(value)
        if (emailInputContainer.isErrorEnabled) {
          val validationResult = validateEmail(value)
          if (validationResult.isSuccessful) {
            emailInputContainer.isErrorEnabled = false
          }
        }
      }

      emailInput.setOnFocusChangeListener { _, hasFocus ->
        hasEditedPhoneOrEmail = true
        if (hasFocus) {
          return@setOnFocusChangeListener
        }
        val validationResult = validateEmail(emailInput.text.toString())
        if (!validationResult.isSuccessful) {
          emailInputContainer.error = getString(validationResult.errorTextKey!!)
        }
      }
    }
  }

  private fun setupPhoneNumberInput() {
    binding.apply {
      phoneNumberInput.onChange { value ->
        viewModel.phoneNumberChanged(value)
        if (phoneNumberInputContainer.isErrorEnabled) {
          val validationResult = validatePhoneNumber(value)
          if (validationResult.isSuccessful) {
            phoneNumberInputContainer.isErrorEnabled = false
          }
        }
      }

      phoneNumberInput.setOnFocusChangeListener { _, hasFocus ->
        hasEditedPhoneOrEmail = true
        if (hasFocus) {
          return@setOnFocusChangeListener
        }
        val validationResult = validatePhoneNumber(phoneNumberInput.text.toString())
        if (!validationResult.isSuccessful) {
          phoneNumberInputContainer.error = getString(validationResult.errorTextKey!!)
        }
      }
    }
  }
}
