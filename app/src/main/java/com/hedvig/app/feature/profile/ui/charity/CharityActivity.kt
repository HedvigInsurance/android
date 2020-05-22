package com.hedvig.app.feature.profile.ui.charity

import android.os.Bundle
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.hedvig.android.owldroid.fragment.CashbackFragment
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_charity.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class CharityActivity : BaseActivity(R.layout.activity_charity) {
    private val tracker: ProfileTracker by inject()

    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.setEdgeToEdgeSystemUiFlags(true)

        setupToolbar(R.id.hedvigToolbar, R.drawable.ic_back, true) {
            onBackPressed()
        }

        loadData()
    }

    private fun loadData() {
        profileViewModel.data.observe(this, Observer { profileData ->
            loadingSpinner.remove()

            profileData?.let { data ->
                data.cashback?.fragments?.cashbackFragment?.let { showSelectedCharity(it) }
                    ?: showCharityPicker(data.cashbackOptions.filterNotNull())
            }
        })
    }

    private fun showSelectedCharity(cashback: CashbackFragment) {
        selectedCharityContainer.show()
        selectCharityContainer.remove()

        Glide
            .with(this)
            .load(cashback.imageUrl)
            .apply(
                RequestOptions().override(
                    Target.SIZE_ORIGINAL,
                    CASH_BACK_IMAGE_HEIGHT
                )
            )
            .into(selectedCharityBanner)

        selectedCharityCardTitle.text = cashback.name
        selectedCharityCardParagraph.text = cashback.paragraph
        charitySelectedHowDoesItWorkButton.setHapticClickListener {
            tracker.howDoesItWorkClick()
            CharityExplanationBottomSheet.newInstance()
                .show(supportFragmentManager, CharityExplanationBottomSheet.TAG)
        }
    }

    private fun showCharityPicker(options: List<ProfileQuery.CashbackOption>) {
        selectCharityContainer.show()
        cashbackOptions.adapter =
            CharityAdapter(options, this) { id ->
                profileViewModel.selectCashback(id)
            }
        selectCharityHowDoesItWorkButton.setHapticClickListener {
            tracker.howDoesItWorkClick()
            CharityExplanationBottomSheet.newInstance()
                .show(supportFragmentManager, CharityExplanationBottomSheet.TAG)
        }
    }

    companion object {
        private const val CASH_BACK_IMAGE_HEIGHT = 200
    }
}
