package com.hedvig.app.feature.marketing.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class StoryPagerAdapter(fragmentManager: FragmentManager, val size: Int) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return StoryFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return size
    }
}
