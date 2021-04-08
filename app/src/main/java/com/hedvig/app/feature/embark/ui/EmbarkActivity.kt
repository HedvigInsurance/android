package com.hedvig.app.feature.embark.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.transition.Transition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExternalRedirectLocation
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityEmbarkBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.NavigationDirection
import com.hedvig.app.feature.embark.passages.UpgradeAppFragment
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerFragment
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerParams
import com.hedvig.app.feature.embark.passages.numberaction.NumberActionFragment
import com.hedvig.app.feature.embark.passages.numberaction.NumberActionParams
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerFragment
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerParameter
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionFragment
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionParameter
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.hedvig.app.feature.embark.passages.textaction.TextActionParameter
import com.hedvig.app.feature.embark.passages.textactionset.TextActionSetFragment
import com.hedvig.app.feature.embark.passages.textactionset.TextActionSetParameter
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.whenApiVersion
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class EmbarkActivity : BaseActivity(R.layout.activity_embark) {
    private val model: EmbarkViewModel by viewModel()
    private val binding by viewBinding(ActivityEmbarkBinding::bind)
    private val marketManager: MarketManager by inject()

    private val storyName: String by lazy {
        intent.getStringExtra(STORY_NAME)
            ?: throw IllegalArgumentException("Programmer error: STORY_NAME not provided to ${this.javaClass.name}")
    }

    private val storyTitle: String by lazy {
        intent.getStringExtra(STORY_TITLE)
            ?: throw IllegalArgumentException("Programmer error: STORY_TITLE not provided to ${this.javaClass.name}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.load(storyName)

        binding.apply {

            whenApiVersion(Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                progressToolbar.toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                    view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
                }
            }

            setupToolbarMenu(progressToolbar)
            progressToolbar.toolbar.title = storyTitle
            
            model.data.observe(this@EmbarkActivity) { embarkData ->
                loadingSpinnerLayout.loadingSpinner.remove()
                setupToolbarMenu(progressToolbar)
                progressToolbar.setProgress(embarkData.progress)

                val passage = embarkData.passage
                actionBar?.title = passage?.name

                if (embarkData.passage?.externalRedirect?.data?.location == EmbarkExternalRedirectLocation.OFFER) {
                    showWebOffer()
                } else {
                    transitionToNextPassage(embarkData.navigationDirection, passage)
                }
            }

            progressToolbar.toolbar.apply {
                setOnMenuItemClickListener(::handleMenuItem)
                setNavigationOnClickListener { finish() }
            }
        }
    }

    private fun handleMenuItem(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.app_settings -> {
            startActivity(SettingsActivity.newInstance(this@EmbarkActivity))
            true
        }
        R.id.app_info -> {
            startActivity(MoreOptionsActivity.newInstance(this@EmbarkActivity))
            true
        }
        R.id.login -> {
            marketManager.market?.openAuth(this, supportFragmentManager)
            true
        }
        R.id.restart -> {
            showRestartDialog()
            true
        }
        R.id.tooltip -> {
            model.data.value?.passage?.tooltips?.let {
                TooltipBottomSheet.newInstance(it, windowManager).show(
                    supportFragmentManager, TooltipBottomSheet.TAG
                )
            }
            true
        }
        else -> false
    }

    private fun showRestartDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_alert_restart_onboarding_title)
            .setMessage(R.string.settings_alert_restart_onboarding_description)
            .setPositiveButton(R.string.ALERT_OK) { _, _ -> model.load(storyName) }
            .setNegativeButton(R.string.ALERT_CANCEL) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupToolbarMenu(progressToolbar: MaterialProgressToolbar) {
        invalidateOptionsMenu()
        progressToolbar.toolbar.menu.clear()

        if (model.data.value?.passage?.tooltips?.isNotEmpty() == true) {
            progressToolbar.toolbar.inflateMenu(R.menu.embark_tooltip_menu)
        } else {
            progressToolbar.toolbar.inflateMenu(R.menu.embark_menu)
        }
    }

    private fun transitionToNextPassage(navigationDirection: NavigationDirection, passage: EmbarkStoryQuery.Passage?) {
        supportFragmentManager
            .findFragmentByTag("passageFragment")
            ?.exitTransition = MaterialSharedAxis(SHARED_AXIS, navigationDirection == NavigationDirection.FORWARDS)

        val newFragment = passageFragment(passage)

        val transition: Transition = when (navigationDirection) {
            NavigationDirection.FORWARDS,
            NavigationDirection.BACKWARDS,
            -> {
                MaterialSharedAxis(SHARED_AXIS, navigationDirection == NavigationDirection.FORWARDS)
            }
            NavigationDirection.INITIAL -> MaterialFadeThrough()
        }

        newFragment.enterTransition = transition

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.passageContainer, newFragment, "passageFragment")
            .commit()
    }

    private fun passageFragment(passage: EmbarkStoryQuery.Passage?): Fragment {
        passage?.action?.asEmbarkSelectAction?.let { options ->
            val parameter = SelectActionParameter.from(
                passage.messages.map { it.fragments.messageFragment.text },
                options.selectData,
                passage.name,
            )
            return SelectActionFragment.newInstance(parameter)
        }

        passage?.action?.asEmbarkTextAction?.let { textAction ->
            val parameter = TextActionParameter.from(
                passage.messages.map { it.fragments.messageFragment.text },
                textAction.textData,
                passage.name
            )
            return TextActionFragment.newInstance(parameter)
        }

        passage?.action?.asEmbarkTextActionSet?.let { textActionSet ->
            textActionSet.textSetData?.let { data ->
                val parameter = TextActionSetParameter.from(
                    passage.messages.map { it.fragments.messageFragment.text },
                    data,
                    passage.name
                )
                return TextActionSetFragment.newInstance(parameter)
            }
        }

        passage?.action?.asEmbarkPreviousInsuranceProviderAction?.let { previousInsuranceAction ->
            val parameter = PreviousInsurerParameter.from(
                passage.messages.map { it.fragments.messageFragment.text },
                previousInsuranceAction
            )
            return PreviousInsurerFragment.newInstance(parameter)
        }

        passage?.action?.asEmbarkNumberAction?.data?.let { numberAction ->
            return NumberActionFragment.newInstance(
                NumberActionParams(
                    passage.messages.map { it.fragments.messageFragment.text },
                    passage.name,
                    numberAction.key,
                    numberAction.placeholder,
                    numberAction.unit,
                    numberAction.label,
                    numberAction.maxValue,
                    numberAction.minValue,
                    numberAction.link.fragments.embarkLinkFragment.name,
                    numberAction.link.fragments.embarkLinkFragment.label,
                )
            )
        }

        passage?.action?.asEmbarkDatePickerAction?.let { datePickerAction ->
            val params = DatePickerParams(
                passage.messages.map { it.fragments.messageFragment.text },
                passage.name,
                datePickerAction.storeKey,
                datePickerAction.label,
                datePickerAction.label,
                datePickerAction.next.fragments.embarkLinkFragment.name
            )
            return DatePickerFragment.newInstance(params)
        }

        return UpgradeAppFragment.newInstance()
    }

    private fun showWebOffer() {
        startActivityForResult(
            WebOnboardingActivity.newNoInstance(
                this@EmbarkActivity,
                "",
                true,
                model.getFromStore("quoteId")
            ),
            REQUEST_OFFER
        )
    }

    override fun onBackPressed() {
        val couldNavigateBack = model.navigateBack()
        if (!couldNavigateBack) {
            super.onBackPressed()
        }
    }

    companion object {
        private const val REQUEST_OFFER = 1

        private const val SHARED_AXIS = MaterialSharedAxis.X
        internal const val STORY_NAME = "STORY_NAME"
        internal const val STORY_TITLE = "STORY_TITLE"
        internal const val PASSAGE_ANIMATION_DELAY_MILLIS = 150L
        internal const val KEY_BOARD_DELAY_MILLIS = 450L

        fun newInstance(context: Context, storyName: String, storyTitle: String) =
            Intent(context, EmbarkActivity::class.java).apply {
                putExtra(STORY_NAME, storyName)
                putExtra(STORY_TITLE, storyTitle)
            }
    }
}
