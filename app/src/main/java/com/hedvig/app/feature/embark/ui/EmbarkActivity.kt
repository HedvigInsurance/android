package com.hedvig.app.feature.embark.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityEmbarkBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.NavigationDirection
import com.hedvig.app.feature.embark.passages.UpgradeAppFragment
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderFragment
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderParameters
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerFragment
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerParams
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionComponent
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionFragment
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionFragment
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionParams
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerFragment
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerParameter
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionFragment
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionParameter
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.hedvig.app.feature.embark.passages.textaction.TextActionParameter
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.hideWithDelay
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.whenApiVersion
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EmbarkActivity : BaseActivity(R.layout.activity_embark) {

    private val storyTitle: String by lazy {
        intent.getStringExtra(STORY_TITLE)
            ?: throw IllegalArgumentException("Programmer error: STORY_TITLE not provided to ${this.javaClass.name}")
    }

    private val storyName: String by lazy {
        intent.getStringExtra(STORY_NAME)
            ?: throw IllegalArgumentException("Programmer error: STORY_NAME not provided to ${this.javaClass.name}")
    }

    private val model: EmbarkViewModel by viewModel { parametersOf(storyName) }
    private val binding by viewBinding(ActivityEmbarkBinding::bind)
    private val marketManager: MarketManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            whenApiVersion(Build.VERSION_CODES.R) {
                window.compatSetDecorFitsSystemWindows(false)
                progressToolbar.applyStatusBarInsets()
            }
            progressToolbar.toolbar.title = storyTitle

            model.viewState.observe(this@EmbarkActivity) { viewState ->
                loadingSpinnerLayout.loadingSpinner.remove()
                setupToolbarMenu(
                    progressToolbar,
                    viewState.hasTooltips,
                    viewState.isLoggedIn
                )
                progressToolbar.setProgress(viewState.progress)

                val passage = viewState.passage
                actionBar?.title = passage?.name

                transitionToNextPassage(viewState.navigationDirection, passage)
            }

            model
                .events
                .flowWithLifecycle(lifecycle)
                .onEach { event ->
                    when (event) {
                        EmbarkViewModel.Event.Chat -> {
                            startActivity(ChatActivity.newInstance(this@EmbarkActivity))
                            fullScreenLoadingSpinnerLayout.hideWithDelay(500)
                        }
                        is EmbarkViewModel.Event.Offer -> {
                            startActivity(
                                OfferActivity.newInstance(
                                    context = this@EmbarkActivity,
                                    quoteIds = event.ids,
                                    shouldShowOnNextAppStart = true
                                )
                            )
                            fullScreenLoadingSpinnerLayout.hideWithDelay(500)
                        }
                        is EmbarkViewModel.Event.Error -> {
                            fullScreenLoadingSpinnerLayout.hide()
                            AlertDialog.Builder(this@EmbarkActivity)
                                .setTitle(R.string.error_dialog_title)
                                .setMessage(event.message ?: getString(R.string.NETWORK_ERROR_ALERT_MESSAGE))
                                .setPositiveButton(R.string.error_dialog_button) { _, _ ->
                                    this@EmbarkActivity.finish()
                                }
                                .create()
                                .show()
                        }
                        is EmbarkViewModel.Event.Loading -> {
                            TransitionManager.beginDelayedTransition(root)
                            fullScreenLoadingSpinnerLayout.isVisible = event.show
                        }
                        EmbarkViewModel.Event.Close -> {
                            fullScreenLoadingSpinnerLayout.hide()
                            finish()
                        }
                    }
                }
                .launchIn(lifecycleScope)

            progressToolbar.toolbar.apply {
                setOnMenuItemClickListener(::handleMenuItem)
                setNavigationOnClickListener { onBackPressed() }
            }
        }
    }

    private fun handleMenuItem(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.login -> {
            marketManager.market?.openAuth(this, supportFragmentManager)
            true
        }
        R.id.exit -> {
            showExitDialog()
            true
        }
        R.id.tooltip -> {
            model.viewState.value?.passage?.tooltips?.let {
                TooltipBottomSheet.newInstance(it).show(
                    supportFragmentManager, TooltipBottomSheet.TAG
                )
            }
            true
        }
        else -> false
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.EMBARK_EXIT_DIALOG_MESSAGE)
            .setPositiveButton(R.string.EMBARK_EXIT_DIALOG_POSITIVE_BUTTON) { _, _ -> finish() }
            .setNegativeButton(R.string.EMBARK_EXIT_DIALOG_NEGATIVE_BUTTON) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupToolbarMenu(
        progressToolbar: MaterialProgressToolbar,
        hasToolTips: Boolean,
        isLoggedIn: Boolean
    ) {
        invalidateOptionsMenu()
        with(progressToolbar.toolbar) {
            menu.clear()
            inflateMenu(R.menu.embark_menu)
            menu.findItem(R.id.tooltip).isVisible = hasToolTips
            menu.findItem(R.id.login).isVisible = !isLoggedIn
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
                val parameter = TextActionParameter.from(
                    passage.messages.map { it.fragments.messageFragment.text },
                    data,
                    passage.name
                )
                return TextActionFragment.newInstance(parameter)
            }
        }

        passage?.action?.asEmbarkPreviousInsuranceProviderAction?.let { previousInsuranceAction ->
            val parameter = PreviousInsurerParameter.from(
                passage.messages.map { it.fragments.messageFragment.text },
                previousInsuranceAction
            )
            return PreviousInsurerFragment.newInstance(parameter)
        }

        passage?.action?.asEmbarkNumberAction?.numberActionData?.let { numberAction ->
            return NumberActionFragment.newInstance(
                NumberActionParams(
                    passage.messages.map { it.fragments.messageFragment.text },
                    passage.name,
                    listOf(
                        NumberActionParams.NumberAction(
                            key = numberAction.fragments.embarkNumberActionFragment.key,
                            title = numberAction.fragments.embarkNumberActionFragment.label,
                            placeholder = numberAction.fragments.embarkNumberActionFragment.placeholder,
                            unit = numberAction.fragments.embarkNumberActionFragment.unit,
                            maxValue = numberAction.fragments.embarkNumberActionFragment.maxValue,
                            minValue = numberAction.fragments.embarkNumberActionFragment.minValue
                        )
                    ),
                    link = numberAction.fragments.embarkNumberActionFragment.link.fragments.embarkLinkFragment.name,
                    submitLabel = numberAction.fragments.embarkNumberActionFragment
                        .link.fragments.embarkLinkFragment.label,
                )
            )
        }

        passage?.action?.asEmbarkNumberActionSet?.numberActionSetData?.let { numberActionSet ->
            return NumberActionFragment.newInstance(
                NumberActionParams(
                    passage.messages.map { it.fragments.messageFragment.text },
                    passage.name,
                    numberActions = numberActionSet.numberActions.map { numberAction ->
                        NumberActionParams.NumberAction(
                            key = numberAction.data!!.key,
                            title = numberAction.data!!.title,
                            placeholder = numberAction.data!!.placeholder,
                            unit = numberAction.data!!.unit,
                            maxValue = numberAction.data!!.maxValue,
                            minValue = numberAction.data!!.minValue
                        )
                    },
                    link = numberActionSet.link.fragments.embarkLinkFragment.name,
                    submitLabel = numberActionSet.link.fragments.embarkLinkFragment.label,
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

        passage?.action?.asEmbarkMultiAction?.let { multiAction ->
            val params = MultiActionParams(
                key = multiAction.multiActionData.key ?: "",
                link = multiAction.multiActionData.link.fragments.embarkLinkFragment.name,
                addLabel = multiAction.multiActionData.addLabel ?: getString(R.string.continue_button),
                maxAmount = multiAction.multiActionData.maxAmount.toInt(),
                messages = passage.messages.map { it.fragments.messageFragment.text },
                passageName = passage.name,
                components = multiAction.multiActionData.components.map {
                    val dropDownActionData = it.asEmbarkDropdownAction?.dropDownActionData
                    val switchActionData = it.asEmbarkSwitchAction?.switchActionData
                    val numberActionData = it.asEmbarkMultiActionNumberAction?.numberActionData

                    when {
                        dropDownActionData != null -> MultiActionComponent.Dropdown(
                            dropDownActionData.key,
                            dropDownActionData.label,
                            dropDownActionData.options.map {
                                MultiActionComponent.Dropdown.Option(it.text, it.value)
                            }
                        )
                        switchActionData != null -> MultiActionComponent.Switch(
                            switchActionData.key,
                            switchActionData.label,
                            switchActionData.defaultValue
                        )

                        numberActionData != null -> MultiActionComponent.Number(
                            numberActionData.key,
                            numberActionData.placeholder,
                            numberActionData.unit,
                            numberActionData.label
                        )
                        else -> throw IllegalArgumentException(
                            "Could not match $it to a component"
                        )
                    }
                },
                submitLabel = multiAction.multiActionData.link.fragments.embarkLinkFragment.label,
            )
            return MultiActionFragment.newInstance(params)
        }

        passage?.action?.asEmbarkAudioRecorderAction?.let { audioRecorderAction ->
            val params = AudioRecorderParameters(
                messages = passage.messages.map { it.fragments.messageFragment.text },
                key = audioRecorderAction.audioRecorderActionData.storeKey,
                label = audioRecorderAction.audioRecorderActionData.label,
                link = audioRecorderAction.audioRecorderActionData.next.fragments.embarkLinkFragment.name,
            )
            return AudioRecorderFragment.newInstance(params)
        }

        return UpgradeAppFragment.newInstance()
    }

    override fun onBackPressed() {
        val couldNavigateBack = model.navigateBack()
        if (!couldNavigateBack) {
            super.onBackPressed()
        }
    }

    companion object {
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
