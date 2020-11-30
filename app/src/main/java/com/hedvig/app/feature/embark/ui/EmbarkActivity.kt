package com.hedvig.app.feature.embark.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityEmbarkBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.SelectActionFragment
import com.hedvig.app.feature.embark.passages.SelectActionPassage
import com.hedvig.app.feature.embark.passages.TextActionData
import com.hedvig.app.feature.embark.passages.TextActionFragment
import com.hedvig.app.feature.embark.passages.UpgradeAppFragment
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
            setSupportActionBar(toolbar)
            supportActionBar?.title = storyName

            model.data.observe(this@EmbarkActivity) { passage ->
                loadingSpinner.loadingSpinner.remove()
                actionBar?.title = passage.name
                passage.action?.asEmbarkSelectAction?.let { options ->
                    val selectActionData = SelectActionPassage.from(
                        passage.messages.map { it.fragments.messageFragment.text },
                        options.data,
                        passage.name
                    )

                    val selectActionFragment = SelectActionFragment.newInstance(selectActionData)

                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.passageContainer, selectActionFragment)
                        .commit()
                    return@observe
                }

                passage.action?.asEmbarkTextAction?.let { textAction ->
                    val textActionData =
                        TextActionData.from(
                            passage.messages.map { it.fragments.messageFragment.text },
                            textAction.data,
                            passage.name
                        )

                    val textActionFragment = TextActionFragment.newInstance(textActionData)
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.passageContainer, textActionFragment)
                        .commit()
                    return@observe
                }

                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.passageContainer, UpgradeAppFragment.newInstance())
                    .commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.embark_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        R.id.moreOptions -> {
            startActivity(MoreOptionsActivity.newInstance(this))
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
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

        fun newInstance(context: Context, storyName: String) =
            Intent(context, EmbarkActivity::class.java).apply {
                putExtra(STORY_NAME, storyName)
            }
    }
}

