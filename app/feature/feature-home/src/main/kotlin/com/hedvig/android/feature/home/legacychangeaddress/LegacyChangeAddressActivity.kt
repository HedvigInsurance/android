package com.hedvig.android.feature.home.legacychangeaddress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.remove
import com.hedvig.android.core.common.android.show
import com.hedvig.android.core.ui.databinding.ListTextItemBinding
import com.hedvig.android.feature.home.R
import com.hedvig.android.feature.home.databinding.LegacyChangeAddressActivityBinding
import com.hedvig.android.navigation.activity.ActivityNavigator
import dev.chrisbanes.insetter.applyInsetter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Only used for non SE markets, and should eventually be not used at all.
 */
class LegacyChangeAddressActivity : AppCompatActivity(R.layout.legacy_change_address_activity) {

  private val activityNavigator: ActivityNavigator by inject()
  private val viewModel: LegacyChangeAddressViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    lifecycle.addObserver(AuthenticatedObserver())
    val binding = LegacyChangeAddressActivityBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))

    with(binding) {
      toolbar.applyInsetter { type(statusBars = true) { padding() } }
      continueButton.applyInsetter { type(navigationBars = true) { margin() } }

      toolbar.setNavigationOnClickListener {
        onBackPressedDispatcher.onBackPressed()
      }
    }

    observeViewModel(binding)
  }

  private fun observeViewModel(binding: LegacyChangeAddressActivityBinding) {
    viewModel.viewState.observe(this) { viewState ->
      TransitionManager.beginDelayedTransition(binding.root)
      setViewState(binding, viewState)
    }
  }

  private fun setViewState(binding: LegacyChangeAddressActivityBinding, viewState: ViewState) {
    when (viewState) {
      ViewState.Loading -> {
        binding.spinner.loadingSpinner.show()
        binding.contentScrollView.remove()
      }
      is ViewState.SelfChangeAddress -> setContent(
        binding = binding,
        titleText = getString(hedvig.resources.R.string.moving_intro_title),
        subtitleText = getString(hedvig.resources.R.string.moving_intro_description),
        buttonText = getString(hedvig.resources.R.string.moving_intro_open_flow_button_text),
        buttonIcon = null,
        onContinue = {
          activityNavigator.navigateToEmbark(
            context = this,
            storyName = viewState.embarkStoryId,
            storyTitle = getString(hedvig.resources.R.string.moving_embark_title),
          )
        },
      )
      ViewState.ManualChangeAddress -> setContent(
        binding = binding,
        titleText = getString(hedvig.resources.R.string.moving_intro_title),
        subtitleText = getString(hedvig.resources.R.string.moving_intro_manual_handling_description),
        buttonText = getString(hedvig.resources.R.string.moving_intro_manual_handling_button_text),
        buttonIcon = hedvig.resources.R.drawable.ic_chat_white,
        onContinue = { activityNavigator.navigateToChat(this) },
      )
      is ViewState.ChangeAddressInProgress -> setUpcomingChangeContent(
        binding = binding,
        titleText = getString(hedvig.resources.R.string.moving_intro_existing_move_title),
        subtitleText = getString(hedvig.resources.R.string.moving_intro_existing_move_description),
        buttonText = getString(hedvig.resources.R.string.moving_intro_manual_handling_button_text),
        buttonIcon = hedvig.resources.R.drawable.ic_chat_white,
        onContinue = { activityNavigator.navigateToChat(this) },
        viewState.upcomingAgreementResult,
      )
      is ViewState.UpcomingAgreementError -> setContent(
        binding = binding,
        titleText = getString(hedvig.resources.R.string.general_error),
        subtitleText = when (viewState.error) {
          GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error.NoContractsError -> {
            "You do not have any contracts eligible for address change"
          }
          is GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error.GeneralError -> {
            viewState.error.message ?: "Could not continue, please try again later"
          }
        },
        buttonText = "Try again",
        buttonIcon = null,
        onContinue = { viewModel.reload() },
      )
      is ViewState.SelfChangeError -> setContent(
        binding = binding,
        titleText = getString(hedvig.resources.R.string.general_error),
        subtitleText = viewState.error.message ?: "Could not continue, please try again later",
        buttonText = "Try again",
        buttonIcon = null,
        onContinue = { viewModel.reload() },
      )
    }
  }

  private fun setContent(
    binding: LegacyChangeAddressActivityBinding,
    titleText: String,
    subtitleText: String,
    buttonText: String?,
    @DrawableRes buttonIcon: Int?,
    onContinue: () -> Unit,
  ) = with(binding) {
    spinner.loadingSpinner.remove()
    contentScrollView.show()
    image.show()
    title.text = titleText
    title.show()
    subtitle.text = subtitleText
    subtitle.show()
    continueButton.text = buttonText
    continueButton.setOnClickListener { onContinue() }
    continueButton.icon = buttonIcon?.let { AppCompatResources.getDrawable(this@LegacyChangeAddressActivity, it) }
    continueButton.isVisible = buttonText != null
  }

  private fun setUpcomingChangeContent(
    binding: LegacyChangeAddressActivityBinding,
    titleText: String,
    subtitleText: String,
    buttonText: String?,
    @DrawableRes buttonIcon: Int?,
    onContinue: () -> Unit,
    upcomingAgreementResult: GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement,
  ) = with(binding) {
    spinner.loadingSpinner.remove()
    contentScrollView.show()
    image.remove()

    title.text = titleText
    title.show()
    subtitle.text = subtitleText
    subtitle.show()

    continueButton.text = buttonText
    continueButton.setOnClickListener { onContinue() }
    continueButton.isVisible = buttonText != null
    continueButton.icon = buttonIcon?.let { AppCompatResources.getDrawable(this@LegacyChangeAddressActivity, it) }

    upcomingAddressLayout.isVisible = upcomingAgreementResult.table?.sections?.isNotEmpty() == true

    upcomingAgreementResult.table.let { table ->
      table?.sections?.forEach { section ->
        section.tableRows.forEach { row ->
          ListTextItemBinding.inflate(layoutInflater, upcomingAddressLayout, false).apply {
            label.text = row.title
            value.text = row.value
            upcomingAddressLayout.addView(this.root)
          }
        }
      }
    }
  }

  companion object {
    fun newInstance(context: Context) = Intent(context, LegacyChangeAddressActivity::class.java)
  }
}
