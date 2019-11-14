package com.hedvig.app.feature.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.storeBoolean
import kotlinx.android.synthetic.main.activity_select_language.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class LanguageSelectionActivity : BaseActivity(R.layout.activity_select_language) {
    private val languageViewModel: LanguageViewModel by viewModel()
    private val tracker: LanguageSelectionTracker by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storeBoolean(HAS_SHOWN_LANGUAGE_SELECTION, true)
        options.adapter = LanguageAdapter(tracker, languageViewModel)
        options.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                compatDrawable(R.drawable.divider)?.let { setDrawable(it) }
            }
        )
    }

    companion object {
        private const val HAS_SHOWN_LANGUAGE_SELECTION = "HAS_SHOWN_LANGUAGE_SELECTION"
        fun newInstance(context: Context) = Intent(context, LanguageSelectionActivity::class.java)
        fun hasBeenShown(context: Context) = context.getStoredBoolean(HAS_SHOWN_LANGUAGE_SELECTION)
    }
}
