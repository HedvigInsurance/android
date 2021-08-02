package com.hedvig.app.feature.profile.ui.charity

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.load
import com.hedvig.android.owldroid.fragment.CashbackFragment
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityCharityBinding
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharityActivity : BaseActivity(R.layout.activity_charity) {
    private val binding by viewBinding(ActivityCharityBinding::bind)
    private val tracker: ProfileTracker by inject()

    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.compatSetDecorFitsSystemWindows(false)

        setupToolbar(R.id.toolbar, R.drawable.ic_back, true) {
            onBackPressed()
        }

        loadData()
    }

    private fun loadData() {
        profileViewModel
            .data
            .flowWithLifecycle(lifecycle)
            .onEach { viewState ->
                binding.loadingSpinner.loadingSpinner.isVisible = viewState is ProfileViewModel.ViewState.Loading

                when (viewState) {
                    is ProfileViewModel.ViewState.Success -> {
                        val selectedCharity = viewState.data.cashback?.fragments?.cashbackFragment
                        if (selectedCharity != null) {
                            showSelectedCharity(selectedCharity)
                        } else {
                            showCharityPicker(viewState.data.cashbackOptions.filterNotNull())
                        }
                    }
                    else -> {
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showSelectedCharity(cashback: CashbackFragment) {
        binding.apply {
            selectedCharityContainer.show()
            selectCharityContainer.remove()
            selectedCharityBanner.load(cashback.imageUrl)

            selectedCharityCardTitle.text = cashback.name
            selectedCharityCardParagraph.text = cashback.description
            charitySelectedHowDoesItWorkButton.setHapticClickListener {
                tracker.howDoesItWorkClick()
                CharityExplanationBottomSheet.newInstance()
                    .show(supportFragmentManager, CharityExplanationBottomSheet.TAG)
            }
        }
    }

    private fun showCharityPicker(options: List<ProfileQuery.CashbackOption>) {
        binding.apply {
            selectCharityContainer.show()
            cashbackOptions.adapter =
                CharityAdapter(this@CharityActivity) { id -> profileViewModel.selectCashback(id) }.also {
                    it.submitList(options)
                }
            selectCharityHowDoesItWorkButton.setHapticClickListener {
                tracker.howDoesItWorkClick()
                CharityExplanationBottomSheet.newInstance()
                    .show(supportFragmentManager, CharityExplanationBottomSheet.TAG)
            }
        }
    }
}
