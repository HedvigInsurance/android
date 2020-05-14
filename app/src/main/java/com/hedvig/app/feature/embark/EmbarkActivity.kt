package com.hedvig.app.feature.embark

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.SelectAction
import com.hedvig.app.feature.embark.passages.SelectActionFragment
import com.hedvig.app.feature.embark.passages.SelectActionPassage
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import e
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.viewmodel.ext.android.viewModel

class EmbarkActivity : BaseActivity(R.layout.activity_embark) {

    private val model: EmbarkViewModel by viewModel()

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

        model.data.observe(this) { data ->
            data?.let { passage ->
                loadingSpinner.remove()

                if (passage.isSelectActionPassage()) {
                    val options = passage.action?.asEmbarkSelectAction ?: return@let
                    val selectActionData = SelectActionPassage(
                        passage.messages.map { it.text },
                        options.data.options.map {
                            SelectAction(
                                it.link.name,
                                it.link.label,
                                it.keys,
                                it.values
                            )
                        }
                    )

                    val selectActionFragment = SelectActionFragment.newInstance(selectActionData)

                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.passageContainer, selectActionFragment)
                        .commit()
                }
            }
        }
    }

    override fun onBackPressed() {
        val couldNavigateBack = model.navigateBack()
        if (!couldNavigateBack) {
            super.onBackPressed()
        }
    }

    companion object {
        internal const val STORY_NAME = "STORY_NAME"

        private fun EmbarkStoryQuery.Passage.isSelectActionPassage() =
            action?.asEmbarkSelectAction != null

        fun newInstance(context: Context, storyName: String) =
            Intent(context, EmbarkActivity::class.java).apply {
                putExtra(STORY_NAME, storyName)
            }
    }
}

