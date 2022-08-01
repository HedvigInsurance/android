package com.hedvig.app.feature.profile.ui.charity

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.compose.AsyncImage
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
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharityActivity : BaseActivity(R.layout.activity_charity) {
  private val binding by viewBinding(ActivityCharityBinding::bind)
  private val profileViewModel: ProfileViewModel by viewModel()
  private val imageLoader: ImageLoader by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getViewModel<CharityViewModel>()
    window.compatSetDecorFitsSystemWindows(false)

    setupToolbar(R.id.toolbar, hedvig.resources.R.drawable.ic_back, true) {
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
          else -> {}
        }
      }
      .launchIn(lifecycleScope)
  }

  private fun showSelectedCharity(cashback: CashbackUiState) {
    binding.apply {
      selectedCharityBanner.setContent {
        Box {
          AsyncImage(
            model = cashback.imageUrl,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = Modifier
              .heightIn(max = 300.dp)
              .align(Alignment.Center)
              .padding(horizontal = 24.dp),
          )
        }
      }
      selectedCharityContainer.show()
      selectCharityContainer.remove()
      selectedCharityCardTitle.text = cashback.name
      selectedCharityCardParagraph.text = cashback.description
      charitySelectedHowDoesItWorkButton.setHapticClickListener {
        ExplanationBottomSheet.newInstance(
          title = getString(hedvig.resources.R.string.CHARITY_INFO_DIALOG_TITLE),
          markDownText = getString(hedvig.resources.R.string.PROFILE_MY_CHARITY_INFO_BODY),
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
        clickListener = { id -> profileViewModel.selectCashback(id) },
      ).also {
        it.submitList(options)
      }
      selectCharityHowDoesItWorkButton.setHapticClickListener {
        ExplanationBottomSheet.newInstance(
          title = getString(hedvig.resources.R.string.CHARITY_INFO_DIALOG_TITLE),
          markDownText = getString(hedvig.resources.R.string.PROFILE_MY_CHARITY_INFO_BODY),
        )
          .show(supportFragmentManager, ExplanationBottomSheet.TAG)
      }
    }
  }
}
