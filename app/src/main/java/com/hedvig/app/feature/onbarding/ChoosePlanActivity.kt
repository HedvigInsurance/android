package com.hedvig.app.feature.onbarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.updatePaddingRelative
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityChoosePlanBinding
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags

class ChoosePlanActivity : BaseActivity(R.layout.activity_choose_plan) {
    private val binding by viewBinding(ActivityChoosePlanBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePaddingRelative(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            setSupportActionBar(toolbar)
            supportActionBar?.title = "TODO Choose Plan"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.choose_plan_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.moreOptions -> {
            makeToast("TODO")
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ChoosePlanActivity::class.java)
    }
}
