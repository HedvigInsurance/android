package com.hedvig.app.feature.dismissablepager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class DismissablePagerAdapter(
    fragmentManager: androidx.fragment.app.FragmentManager,
    private val data: List<DismissablePagerPage>
) : androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int) = if (position < data.size) {
        data[position].let { page ->
            DismissablePageFragment.newInstance(page.imageUrl, page.title, page.paragraph)
        }
    } else {
        androidx.fragment.app.Fragment()
    }

    override fun getCount() = data.size + 1
}
