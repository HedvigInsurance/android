package com.hedvig.app.feature.home.ui.changeaddress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ChangeAddressActivityBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error.GeneralError
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error.NoContractsError
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.ChangeAddressInProgress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.Loading
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.ManualChangeAddress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.SelfChangeAddress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.SelfChangeError
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.UpcomingAgreementError
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.format.DateTimeFormatter

class ChangeAddressActivity : BaseActivity(R.layout.change_address_activity) {

    private val binding by viewBinding(ChangeAddressActivityBinding::bind)
    private val viewModel: ChangeAddressViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            root.setEdgeToEdgeSystemUiFlags(true)

            Insetter.builder().setOnApplyInsetsListener { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }.applyToView(toolbar)

            Insetter.builder().setOnApplyInsetsListener { view, insets, initialState ->
                view.updateMargin(bottom = initialState.paddings.bottom + insets.stableInsetBottom)
            }.applyToView(continueButton)

            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.viewState.observe(this) { viewState ->
            TransitionManager.beginDelayedTransition(binding.root)
            setViewState(viewState)
        }
    }

    private fun setViewState(viewState: ViewState): Any = when (viewState) {
        Loading -> {
            binding.spinner.loadingSpinner.show()
            binding.contentScrollView.remove()
        }
        SelfChangeAddress -> setContent(
            titleText = getString(R.string.moving_intro_title),
            subtitleText = getString(R.string.moving_intro_description),
            buttonText = getString(R.string.moving_intro_open_flow_button_text),
            buttonIcon = null,
            // TODO: Start Embark
            onContinue = { }
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
            onContinue = { viewModel.reload() }
        )
        is SelfChangeError -> setContent(
            titleText = getString(R.string.error_dialog_title),
            subtitleText = viewState.error.message ?: "Could not continue, please try again later",
            buttonText = "Try again",
            buttonIcon = null,
            onContinue = { viewModel.reload() }
        )
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
        continueButton.setOnClickListener { onContinue() }
        continueButton.icon = buttonIcon?.let { ContextCompat.getDrawable(this@ChangeAddressActivity, it) }
        continueButton.isVisible = buttonText != null
    }

    private fun setUpcomingChangeContent(
        titleText: String,
        subtitleText: String,
        buttonText: String?,
        @DrawableRes buttonIcon: Int?,
        onContinue: () -> Unit,
        upcomingAgreementResult: UpcomingAgreement
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
        continueButton.icon = buttonIcon?.let { ContextCompat.getDrawable(this@ChangeAddressActivity, it) }

        upcomingAddressLayout.upcomingAddressLayoutRoot.show()
        upcomingAddressLayout.addressLabel.text = upcomingAgreementResult.address.street
        upcomingAddressLayout.postalCodeLabel.text = upcomingAgreementResult.address.postalCode
        upcomingAddressLayout.typeLabel.text = upcomingAgreementResult.addressType?.let(::getString) ?: "-"
        upcomingAddressLayout.livingSpaceLabel.text = getString(R.string.HOUSE_INFO_BIYTA_SQUAREMETERS, upcomingAgreementResult.squareMeters)
        upcomingAddressLayout.dateLabel.text = upcomingAgreementResult.activeFrom?.format(DateTimeFormatter.ISO_DATE)
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ChangeAddressActivity::class.java)
    }
}
