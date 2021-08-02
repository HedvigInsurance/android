package com.hedvig.app.feature.onboarding.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
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
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.view.applyNavigationBarInsetsMargin
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChoosePlanActivity : BaseActivity(R.layout.activity_choose_plan) {
    private val binding by viewBinding(ActivityChoosePlanBinding::bind)
    private val marketProvider: MarketManager by inject()
    private val model: ChoosePlanViewModel by viewModel()
    private val marketManager: MarketManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            toolbar.applyStatusBarInsets()
            continueButton.applyNavigationBarInsetsMargin()
            recycler.applyNavigationBarInsetsMargin()

            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener { onBackPressed() }

            recycler.itemAnimator = ViewHolderReusingDefaultItemAnimator()
            val adapter = OnboardingAdapter(model, marketProvider)
            recycler.adapter = adapter

            continueButton.setHapticClickListener {
                val storyName = model.selectedQuoteType.value?.embarkStory?.name
                    ?: throw IllegalArgumentException("No story name found")
                val storyTitle = model.selectedQuoteType.value?.embarkStory?.title
                    ?: throw IllegalArgumentException("No story title found")
                startActivity(EmbarkActivity.newInstance(this@ChoosePlanActivity, storyName, storyTitle))
            }
            model.selectedQuoteType.observe(this@ChoosePlanActivity) { selected ->
                val data = (model.data.value as? ChoosePlanViewModel.ViewState.Success ?: return@observe)
                val bundles = data.data.map {
                    OnboardingModel.Bundle(
                        selected = it.name == selected.embarkStory.name,
                        embarkStory = it
                    )
                }
                adapter.submitList(
                    bundles
                )
            }
            model
                .data
                .flowWithLifecycle(lifecycle)
                .onEach { viewState ->
                    continueButton.isVisible = viewState is ChoosePlanViewModel.ViewState.Success
                    when (viewState) {
                        ChoosePlanViewModel.ViewState.Error -> {
                            adapter.submitList(
                                listOf(
                                    OnboardingModel.Error
                                )
                            )
                        }
                        ChoosePlanViewModel.ViewState.Loading -> {
                        }
                        is ChoosePlanViewModel.ViewState.Success -> {
                            val bundles = viewState.data
                            getMobileTypesNew(bundles).find { it.selected }?.let {
                                model.setSelectedQuoteType(it)
                            }
                            adapter.submitList(
                                getMobileTypesNew(bundles)
                            )
                        }
                    }
                }
                .launchIn(lifecycleScope)
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
