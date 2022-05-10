package com.hedvig.app.feature.profile.ui.charity

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.load
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityCharityBinding
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.tab.CashbackUiState
import com.hedvig.app.feature.profile.ui.tab.CharityOption
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharityActivity : BaseActivity(R.layout.activity_charity) {
    private val binding by viewBinding(ActivityCharityBinding::bind)

    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getViewModel<CharityViewModel>()
        window.compatSetDecorFitsSystemWindows(false)

        setupToolbar(R.id.toolbar, R.drawable.ic_back, true) {
            onBackPressed()
        }

        profileViewModel
            .data
            .flowWithLifecycle(lifecycle)
            .onEach { viewState ->
                binding.loadingSpinner.loadingSpinner.isVisible = viewState is ProfileViewModel.ViewState.Loading

                when (viewState) {
                    is ProfileViewModel.ViewState.Success -> {
                        if (viewState.profileUiState.cashbackUiState != null) {
                            showSelectedCharity(viewState.profileUiState.cashbackUiState)
                        } else {
                            showCharityPicker(viewState.profileUiState.charityOptions)
                        }
                    }
                    else -> {
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showSelectedCharity(cashback: CashbackUiState) {
        binding.apply {
            selectedCharityContainer.show()
            selectCharityContainer.remove()
            selectedCharityBanner.load(cashback.imageUrl)

            selectedCharityCardTitle.text = cashback.name
            selectedCharityCardParagraph.text = cashback.description
            charitySelectedHowDoesItWorkButton.setHapticClickListener {
                ExplanationBottomSheet.newInstance(
                    title = getString(R.string.CHARITY_INFO_DIALOG_TITLE),
                    markDownText = getString(R.string.PROFILE_MY_CHARITY_INFO_BODY),
                )
                    .show(supportFragmentManager, ExplanationBottomSheet.TAG)
            }
        }
    }

    private fun showCharityPicker(options: List<CharityOption>) {
        binding.apply {
            selectCharityContainer.show()
            cashbackOptions.adapter = CharityAdapter(
                context = this@CharityActivity,
                clickListener = { id -> profileViewModel.selectCashback(id) }
            ).also {
                it.submitList(options)
            }
            selectCharityHowDoesItWorkButton.setHapticClickListener {
                ExplanationBottomSheet.newInstance(
                    title = getString(R.string.CHARITY_INFO_DIALOG_TITLE),
                    markDownText = getString(R.string.PROFILE_MY_CHARITY_INFO_BODY),
                )
                    .show(supportFragmentManager, ExplanationBottomSheet.TAG)
            }
        }
    }
}
