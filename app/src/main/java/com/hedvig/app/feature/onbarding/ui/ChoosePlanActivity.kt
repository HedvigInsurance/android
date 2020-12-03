package com.hedvig.app.feature.onbarding.ui

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
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.onbarding.ChoosePlanViewModel
import com.hedvig.app.feature.onbarding.OnboardingModel
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ChoosePlanActivity : BaseActivity(R.layout.activity_choose_plan) {
    private val binding by viewBinding(ActivityChoosePlanBinding::bind)
    private val marketProvider: MarketProvider by inject()
    private val viewModel: ChoosePlanViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            recycler.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

            recycler.itemAnimator = ViewHolderReusingDefaultItemAnimator()
            recycler.adapter = OnboardingAdapter(viewModel, marketProvider)
            viewModel.data.observe(this@ChoosePlanActivity) { response ->
                if (response.isFailure) {
                    (recycler.adapter as OnboardingAdapter).submitList(listOf(OnboardingModel.Error))
                    return@observe
                }
                val bundles = response.getOrNull()?.embarkStories ?: return@observe
                getMobileTypes(bundles).find { it is OnboardingModel.Quote.Bundle }?.let {
                    viewModel.setSelectedQuoteType(
                        it
                    )
                }
                (recycler.adapter as OnboardingAdapter).submitList(
                    listOfNotNull(
                        *getMobileTypes(bundles).toTypedArray(),
                        OnboardingModel.Info,
                        OnboardingModel.Button
                    )
                )
            }
            viewModel.load()
            viewModel.selectedQuoteType.observe(this@ChoosePlanActivity) { quote ->
                val data = viewModel.data.value?.getOrNull()?.embarkStories
                (recycler.adapter as OnboardingAdapter).submitList(
                    listOf(
                        data?.find { it.name.contains(COMBO) }?.let { embarkStory ->
                            OnboardingModel.Quote.Bundle(
                                selected = quote is OnboardingModel.Quote.Bundle,
                                embarkStory = embarkStory
                            )
                        },
                        data?.find { it.name.contains(CONTENTS) }?.let { embarkStory ->
                            OnboardingModel.Quote.Content(
                                selected = quote is OnboardingModel.Quote.Content,
                                embarkStory = embarkStory
                            )
                        },
                        data?.find { it.name.contains(TRAVEL) }?.let { embarkStory ->
                            OnboardingModel.Quote.Travel(
                                selected = quote is OnboardingModel.Quote.Travel,
                                embarkStory = embarkStory
                            )
                        },
                        OnboardingModel.Info,
                        OnboardingModel.Button
                    )
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.choose_plan_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.moreOptions -> {
            startActivity(MoreOptionsActivity.newInstance(this))
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val COMBO = "Combo"
        private const val CONTENTS = "Contents"
        private const val TRAVEL = "Travel"

        fun newInstance(context: Context) = Intent(context, ChoosePlanActivity::class.java)

        private fun getMobileTypes(bundles: List<ChoosePlanQuery.EmbarkStory>) =
            bundles.filter { it.type == EmbarkStoryType.APP_ONBOARDING }.map { embarkStory ->
                when {
                    embarkStory.name.contains(COMBO) -> {
                        OnboardingModel.Quote.Bundle(true, embarkStory)
                    }
                    embarkStory.name.contains(CONTENTS) -> {
                        OnboardingModel.Quote.Content(false, embarkStory)
                    }
                    embarkStory.name.contains(TRAVEL) -> {
                        OnboardingModel.Quote.Travel(false, embarkStory)
                    }
                    else -> {
                        null
                    }
                }
            }
    }
}
