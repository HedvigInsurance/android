package com.hedvig.app.feature.insurance.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailActivityBinding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e

class ContractDetailActivity : BaseActivity(R.layout.contract_detail_activity) {
    private val binding by viewBinding(ContractDetailActivityBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.apply {
                doOnApplyWindowInsets { view, insets, initialState ->
                    view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
                }
                setNavigationOnClickListener {
                    onBackPressed()
                }
            }
            tabContent.adapter = ContractDetailTabAdapter(this@ContractDetailActivity)
            TabLayoutMediator(tabContainer, tabContent) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = "Your info"
                    }
                    1 -> {
                        tab.text = "Coverage"
                    }
                    2 -> {
                        tab.text = "Documents"
                    }
                    else -> {
                        e { "Invalid tab index: $position" }
                    }
                }
            }.attach()
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ContractDetailActivity::class.java)
    }
}

class ContractDetailTabAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int) = Fragment()
}
