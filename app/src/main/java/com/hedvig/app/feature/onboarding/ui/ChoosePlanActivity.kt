package com.hedvig.app.feature.onboarding.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.updatePaddingRelative
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.EmbarkStoryType
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityChoosePlanBinding
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.onboarding.ChoosePlanViewModel
import com.hedvig.app.feature.onboarding.OnboardingModel
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class ChoosePlanActivity : BaseActivity(R.layout.activity_choose_plan) {
    private val binding by viewBinding(ActivityChoosePlanBinding::bind)
    private val marketProvider: MarketManager by inject()
    private val viewModel: ChoosePlanViewModel by viewModel()
    private val marketManager: MarketManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            continueButton.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
            }
            recycler.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener { onBackPressed() }

            recycler.itemAnimator = ViewHolderReusingDefaultItemAnimator()
            recycler.adapter = OnboardingAdapter(viewModel, marketProvider)

            continueButton.setHapticClickListener {
                when (val storyName = viewModel.selectedQuoteType.value?.embarkStory?.name) {
                    NO_ENGLISH_TRAVEL_STORY_NAME, NO_NORWEGIAN_TRAVEL_STORY_NAME -> {
                        startActivity(EmbarkActivity.newInstance(this@ChoosePlanActivity, storyName))
                    }
                    null -> Timber.e("Could not start embark activity - null story name")
                    else -> {
                        startActivity(
                            WebOnboardingActivity.newNoInstance(
                                this@ChoosePlanActivity,
                                viewModel.getWebPath()
                            )
                        )
                    }
                }
            }
            viewModel.data.observe(this@ChoosePlanActivity) { response ->
                val bundles = response.getOrNull()
                if (response.isFailure || bundles == null) {
                    (recycler.adapter as OnboardingAdapter).submitList(listOf(OnboardingModel.Error))
                    continueButton.remove()
                    return@observe
                }
                continueButton.show()
                getMobileTypesNew(bundles).find { it.selected }?.let {
                    viewModel.setSelectedQuoteType(it)
                }
                (recycler.adapter as OnboardingAdapter).submitList(
                    listOfNotNull(
                        *getMobileTypesNew(bundles).toTypedArray()
                    )
                )
            }
            viewModel.load()
            viewModel.selectedQuoteType.observe(this@ChoosePlanActivity) { selected ->
                val data = viewModel.data.value?.getOrNull()
                val bundles = data?.map {
                    OnboardingModel.Bundle(
                        selected = it.name == selected.embarkStory.name,
                        embarkStory = it
                    )
                }
                (recycler.adapter as OnboardingAdapter).submitList(
                    bundles?.let {
                        listOfNotNull(*it.toTypedArray())
                    }
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.choose_plan_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.app_settings -> {
            startActivity(SettingsActivity.newInstance(this))
            true
        }
        R.id.app_info -> {
            startActivity(MoreOptionsActivity.newInstance(this))
            true
        }
        R.id.login -> {
            marketManager.market?.openAuth(this, supportFragmentManager)
            true
        }
        else -> false
    }

    companion object {

        const val COMBO = "Combo"
        const val CONTENTS = "Contents"
        const val TRAVEL = "Travel"

        internal const val NO_ENGLISH_TRAVEL_STORY_NAME = "Web Onboarding NO - English Travel"
        internal const val NO_NORWEGIAN_TRAVEL_STORY_NAME = "Web Onboarding NO - Norwegian Travel"

        fun newInstance(context: Context) = Intent(context, ChoosePlanActivity::class.java)

        private fun getMobileTypesNew(bundles: List<ChoosePlanQuery.EmbarkStory>) =
            bundles.filter { it.type == EmbarkStoryType.APP_ONBOARDING }.map { embarkStory ->
                if (embarkStory.name.contains(COMBO)) {
                    OnboardingModel.Bundle(true, embarkStory)
                } else {
                    OnboardingModel.Bundle(false, embarkStory)
                }
            }
    }
}
