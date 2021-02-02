package com.hedvig.app.feature.embark.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.transition.Transition
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityEmbarkBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.NavigationDirection
import com.hedvig.app.feature.embark.passages.UpgradeAppFragment
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
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.viewBinding
import e
import org.koin.android.viewmodel.ext.android.viewModel

class EmbarkActivity : BaseActivity(R.layout.activity_embark) {
    private val model: EmbarkViewModel by viewModel()
    private val binding by viewBinding(ActivityEmbarkBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storyName = intent.getStringExtra(STORY_NAME)

        if (storyName == null) {
            // TODO: Implement error UI that design must provide
            e { "Programmer error: STORY_NAME not provided to ${this.javaClass.name}" }
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        model.load(storyName)

        binding.apply {
            progressToolbar.toolbar.apply {
                title = storyName
                setNavigationOnClickListener {
                    finish()
                }

                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.moreOptions -> {
                            startActivity(MoreOptionsActivity.newInstance(this@EmbarkActivity))
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
                }
            }

            model.data.observe(this@EmbarkActivity) { embarkData ->
                invalidateOptionsMenu()
                progressToolbar.toolbar.menu.clear()
                if (model.data.value?.passage?.tooltips?.isNotEmpty() == true) {
                    progressToolbar.toolbar.inflateMenu(R.menu.embark_tooltip_menu)
                } else {
                    progressToolbar.toolbar.inflateMenu(R.menu.embark_menu)
                }

                loadingSpinner.loadingSpinner.remove()
                progressToolbar.setProgress(embarkData.progress)

                val passage = embarkData.passage
                actionBar?.title = passage?.name

                supportFragmentManager.findFragmentById(R.id.passageContainer)?.exitTransition =
                    MaterialSharedAxis(SHARED_AXIS,
                        embarkData.navigationDirection == NavigationDirection.FORWARDS)

                val newFragment = passageFragment(passage)

                val transition: Transition = when (embarkData.navigationDirection) {
                    NavigationDirection.FORWARDS,
                    NavigationDirection.BACKWARDS,
                    -> {
                        MaterialSharedAxis(SHARED_AXIS,
                            embarkData.navigationDirection == NavigationDirection.FORWARDS)
                    }
                    NavigationDirection.INITIAL -> MaterialFadeThrough()
                }

                newFragment.enterTransition = transition

                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.passageContainer, newFragment)
                    .commit()
            }
        }
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

        fun newInstance(context: Context, storyName: String) =
            Intent(context, EmbarkActivity::class.java).apply {
                putExtra(STORY_NAME, storyName)
            }
    }
}
