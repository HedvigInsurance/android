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
import com.hedvig.app.feature.embark.passages.SelectActionFragment
import com.hedvig.app.feature.embark.passages.SelectActionPassage
import com.hedvig.app.feature.embark.passages.TextActionData
import com.hedvig.app.feature.embark.passages.TextActionFragment
import com.hedvig.app.feature.embark.passages.TextActionSetData
import com.hedvig.app.feature.embark.passages.TextActionSetFragment
import com.hedvig.app.feature.embark.passages.UpgradeAppFragment
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.synthetic.main.activity_embark.*
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
            val selectActionData = SelectActionPassage.from(
                passage.messages.map { it.fragments.messageFragment.text },
                options.data,
                passage.name,
            )

            return SelectActionFragment.newInstance(selectActionData)
        }

        passage?.action?.asEmbarkTextAction?.let { textAction ->
            val textActionData =
                TextActionData.from(
                    passage.messages.map { it.fragments.messageFragment.text },
                    textAction.data,
                    passage.name
                )

            return TextActionFragment.newInstance(textActionData)
        }

        passage?.action?.asEmbarkTextActionSet?.let { textActionSet ->
            textActionSet.data?.let { data ->
                val textActionSetData =
                    TextActionSetData.from(
                        passage.messages.map { it.fragments.messageFragment.text },
                        data,
                        passage.name
                    )
                return TextActionSetFragment.newInstance(textActionSetData)
            }
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

