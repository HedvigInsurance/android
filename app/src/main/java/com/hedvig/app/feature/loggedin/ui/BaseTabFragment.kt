package com.hedvig.app.feature.loggedin.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.hedvig.app.R

abstract class BaseTabFragment : androidx.fragment.app.Fragment() {
    val navController by lazy { requireActivity().findNavController(R.id.loggedNavigationHost) }

    @get:LayoutRes
    abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(layout, container, false)

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            view?.let { v ->
                (v as? NestedScrollView)?.scrollTo(0, 0)
                (v as? androidx.recyclerview.widget.RecyclerView)?.let { recyclerView ->
                    recyclerView.scrollToPosition(0)
                    (recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)?.scrollToPositionWithOffset(
                        0,
                        0
                    )
                }
                (v as? androidx.coordinatorlayout.widget.CoordinatorLayout)?.let { coordinatorLayout ->
                    (coordinatorLayout.getChildAt(0) as? NestedScrollView)?.let {
                        it.scrollTo(0, 0)
                    }
                    (coordinatorLayout.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView)?.let { recyclerView ->
                        recyclerView.scrollToPosition(0)
                        (recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)?.scrollToPositionWithOffset(
                            0,
                            0
                        )
                    }
                }
            }
        }
    }
}
