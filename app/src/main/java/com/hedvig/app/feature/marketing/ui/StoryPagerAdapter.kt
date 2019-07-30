package com.hedvig.app.feature.marketing.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class StoryPagerAdapter(fragmentManager: androidx.fragment.app.FragmentManager, val size: Int) :
    androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return StoryFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return size
    }
}
