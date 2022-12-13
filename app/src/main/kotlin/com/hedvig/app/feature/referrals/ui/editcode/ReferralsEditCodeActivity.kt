package com.hedvig.app.feature.referrals.ui.editcode

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityReferralsEditCodeBinding
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import slimber.log.e

class ReferralsEditCodeActivity : AppCompatActivity(R.layout.activity_referrals_edit_code) {
  private val binding by viewBinding(ActivityReferralsEditCodeBinding::bind)
  private val viewModel: ReferralsEditCodeViewModel by viewModel()

  private var isSubmitting = false
  private var dirty = false

  @SuppressLint("InflateParams")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding.apply {
      window.compatSetDecorFitsSystemWindows(false)

      toolbar.doOnLayout { applyInsets(it.height) }
      toolbar.applyStatusBarInsets()

      scrollView.applyNavigationBarInsets()

      toolbar.setNavigationOnClickListener {
        onBackPressed()
      }
      toolbar.setOnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
          R.id.save -> {
            toolbar.dismissKeyboard()
            submit()
            true
          }
          else -> false
        }
      }

      val currentCode = intent.getStringExtra(CODE)

      if (currentCode == null) {
        e { "Programmer error: `CODE` not passed to ${this.javaClass.name}" }
      }

      code.setText(currentCode)
      code.onChange { newValue ->
        viewModel.setIsDirty()
        when (validate(newValue)) {
          ValidationResult.VALID -> {
            toolbar.menu.findItem(R.id.save).isEnabled = true
            codeContainer.error = null
          }
          ValidationResult.TOO_SHORT -> {
            toolbar.menu.findItem(R.id.save).isEnabled = false
            codeContainer.error = null
          }
          ValidationResult.TOO_LONG -> {
            toolbar.menu.findItem(R.id.save).isEnabled = false
            codeContainer.error =
              getString(hedvig.resources.R.string.referrals_change_code_sheet_error_max_length)
          }
        }
      }
      code.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          code.dismissKeyboard()
          submit()
          return@setOnEditorActionListener true
        }
        false
      }

      viewModel.isSubmitting.observe(this@ReferralsEditCodeActivity) { iss ->
        isSubmitting = iss

        toolbar.menu.findItem(R.id.save).let { save ->
          if (isSubmitting) {
            save.actionView = layoutInflater.inflate(
              R.layout.toolbar_loading_spinner,
              null,
            )
            save.isEnabled = false
          } else {
            save.actionView = null
            save.isEnabled = true
          }
        }
      }
      viewModel.dirty.observe(this@ReferralsEditCodeActivity) { dirty = it }

      viewModel
        .data
        .flowWithLifecycle(lifecycle)
        .onEach { viewState ->
          codeContainer.error = if (viewState is ReferralsEditCodeViewModel.ViewState.Error) {
            getString(hedvig.resources.R.string.referrals_change_code_sheet_general_error)
          } else {
            null
          }

          when (viewState) {
            is ReferralsEditCodeViewModel.ViewState.Success -> {
              val urcc = viewState.data.updateReferralCampaignCode

              urcc.asSuccessfullyUpdatedCode?.let {
                codeContainer.error = null
                finish()
                return@onEach
              }
              urcc.asCodeAlreadyTaken?.let {
                codeContainer.error =
                  getString(hedvig.resources.R.string.referrals_change_code_sheet_error_claimed_code)
                return@onEach
              }
              urcc.asCodeTooShort?.let {
                codeContainer.error =
                  getString(hedvig.resources.R.string.referrals_change_code_sheet_general_error)
                return@onEach
              }
              urcc.asCodeTooLong?.let {
                codeContainer.error =
                  getString(hedvig.resources.R.string.referrals_change_code_sheet_error_max_length)
                return@onEach
              }
              urcc.asExceededMaximumUpdates?.maximumNumberOfUpdates?.let { maximumNumberOfUpdates ->
                codeContainer.error =
                  getString(
                    hedvig.resources.R.string.referrals_change_code_sheet_error_change_limit_reached,
                    maximumNumberOfUpdates,
                  )
                return@onEach
              }

              codeContainer.error =
                getString(hedvig.resources.R.string.referrals_change_code_sheet_general_error)
            }
            else -> {
            }
          }
        }
        .launchIn(lifecycleScope)
    }
  }

  override fun onBackPressed() {
    if (isSubmitting) {
      return
    }
    if (dirty) {
      showAlert(
        hedvig.resources.R.string.referrals_edit_code_confirm_dismiss_title,
        hedvig.resources.R.string.referrals_edit_code_confirm_dismiss_body,
        hedvig.resources.R.string.referrals_edit_code_confirm_dismiss_continue,
        hedvig.resources.R.string.referrals_edit_code_confirm_dismiss_cancel,
        positiveAction = {
          super.onBackPressed()
        },
        negativeAction = {},
      )
    } else {
      super.onBackPressed()
    }
  }

  private fun submit() {
    if (isSubmitting) {
      return
    }
    val enteredCode = binding.code.text.toString()
    if (validate(enteredCode) == ValidationResult.VALID) {
      viewModel.changeCode(enteredCode)
    }
  }

  private fun applyInsets(toolbarHeight: Int) {
    binding.scrollView.updatePadding(top = toolbarHeight)
  }

  companion object {
    private enum class ValidationResult {
      VALID,
      TOO_SHORT,
      TOO_LONG,
    }

    private fun validate(code: String): ValidationResult {
      if (code.isBlank()) {
        return ValidationResult.TOO_SHORT
      }

      if (code.length >= 24) {
        return ValidationResult.TOO_LONG
      }

      return ValidationResult.VALID
    }

    private const val CODE = "CODE"
    fun newInstance(context: Context, currentCode: String) =
      Intent(context, ReferralsEditCodeActivity::class.java).apply {
        putExtra(CODE, currentCode)
      }
  }
}
