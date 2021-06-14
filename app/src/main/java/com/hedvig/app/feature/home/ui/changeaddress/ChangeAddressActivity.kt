package com.hedvig.app.feature.home.ui.changeaddress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ChangeAddressActivityBinding
import com.hedvig.app.databinding.ListTextItemBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error.GeneralError
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error.NoContractsError
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.ChangeAddressInProgress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.Loading
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.ManualChangeAddress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.SelfChangeAddress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.SelfChangeError
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.UpcomingAgreementError
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsetsMargin
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeAddressActivity : BaseActivity(R.layout.change_address_activity) {

    private val binding by viewBinding(ChangeAddressActivityBinding::bind)
    private val model: ChangeAddressViewModelImpl by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            window.compatSetDecorFitsSystemWindows(false)
            toolbar.applyStatusBarInsets()
            continueButton.applyNavigationBarInsetsMargin()

            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        model.viewState.observe(this) { viewState ->
            TransitionManager.beginDelayedTransition(binding.root)
            setViewState(viewState)
        }
    }

    private fun setViewState(viewState: ViewState) {
        when (viewState) {
            Loading -> {
                binding.spinner.loadingSpinner.show()
                binding.contentScrollView.remove()
            }
            is SelfChangeAddress -> setContent(
                titleText = getString(R.string.moving_intro_title),
                subtitleText = getString(R.string.moving_intro_description),
                buttonText = getString(R.string.moving_intro_open_flow_button_text),
                buttonIcon = null,
                onContinue = {
                    startActivity(
                        EmbarkActivity.newInstance(
                            context = this,
                            storyName = viewState.embarkStoryId,
                            storyTitle = getString(R.string.moving_embark_title)
                        )
                    )
                }
            )
            ManualChangeAddress -> setContent(
                titleText = getString(R.string.moving_intro_title),
                subtitleText = getString(R.string.moving_intro_manual_handling_description),
                buttonText = getString(R.string.moving_intro_manual_handling_button_text),
                buttonIcon = R.drawable.ic_chat_white,
                onContinue = { openChat() }
            )
            is ChangeAddressInProgress -> setUpcomingChangeContent(
                titleText = getString(R.string.moving_intro_existing_move_title),
                subtitleText = getString(R.string.moving_intro_existing_move_description),
                buttonText = getString(R.string.moving_intro_manual_handling_button_text),
                buttonIcon = R.drawable.ic_chat_white,
                onContinue = { openChat() },
                viewState.upcomingAgreementResult
            )
            is UpcomingAgreementError -> setContent(
                titleText = getString(R.string.error_dialog_title),
                subtitleText = when (viewState.error) {
                    NoContractsError -> "You do not have any contracts eligible for address change"
                    is GeneralError -> viewState.error.message ?: "Could not continue, please try again later"
                },
                buttonText = "Try again",
                buttonIcon = null,
                onContinue = { model.reload() }
            )
            is SelfChangeError -> setContent(
                titleText = getString(R.string.error_dialog_title),
                subtitleText = viewState.error.message ?: "Could not continue, please try again later",
                buttonText = "Try again",
                buttonIcon = null,
                onContinue = { model.reload() }
            )
        }
    }

    private fun openChat() = startActivity(ChatActivity.newInstance(this, true))

    private fun setContent(
        titleText: String,
        subtitleText: String,
        buttonText: String?,
        @DrawableRes buttonIcon: Int?,
        onContinue: () -> Unit
    ) = with(binding) {
        spinner.loadingSpinner.remove()
        contentScrollView.show()
        image.show()
        title.text = titleText
        title.show()
        subtitle.text = subtitleText
        subtitle.show()
        continueButton.text = buttonText
        continueButton.setHapticClickListener { onContinue() }
        continueButton.icon = buttonIcon?.let { compatDrawable(it) }
        continueButton.isVisible = buttonText != null
    }

    private fun setUpcomingChangeContent(
        titleText: String,
        subtitleText: String,
        buttonText: String?,
        @DrawableRes buttonIcon: Int?,
        onContinue: () -> Unit,
        upcomingAgreementResult: GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
    ) = with(binding) {
        spinner.loadingSpinner.remove()
        contentScrollView.show()
        image.remove()

        title.text = titleText
        title.show()
        subtitle.text = subtitleText
        subtitle.show()

        continueButton.text = buttonText
        continueButton.setHapticClickListener { onContinue() }
        continueButton.isVisible = buttonText != null
        continueButton.icon = buttonIcon?.let { compatDrawable(it) }

        upcomingAddressLayout.isVisible = upcomingAgreementResult.table?.sections?.isNotEmpty() == true

        upcomingAgreementResult.table.let { table ->
            table?.sections?.forEach { section ->
                section.rows.forEach { row ->
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
        fun newInstance(context: Context) = Intent(context, ChangeAddressActivity::class.java)
    }
}
