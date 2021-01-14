package com.hedvig.app.feature.keygear.ui.tab

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.transition.MaterialFadeThrough
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.BASE_MARGIN_QUINTUPLE
import com.hedvig.app.BASE_MARGIN_TRIPLE
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentKeyGearBinding
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemActivity
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.ui.animator.SlideInItemAnimator
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.transitionPair
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class KeyGearFragment : Fragment(R.layout.fragment_key_gear) {

    private val viewModel: KeyGearViewModel by sharedViewModel()
    private val tracker: KeyGearTracker by inject()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentKeyGearBinding::bind)
    private var scroll = 0

    private var hasSentAutoAddedItems = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scroll = 0

        with(binding) {
            val scrollInitialTopPadding = keyGearRoot.paddingTop
            loggedInViewModel.toolbarInset.observe(viewLifecycleOwner) { toolbarInsets ->
                keyGearRoot.updatePadding(top = scrollInitialTopPadding + toolbarInsets)
            }

            val scrollInitialBottomPadding = keyGearRoot.paddingBottom
            loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bottomTabInset ->
                keyGearRoot.updatePadding(bottom = scrollInitialBottomPadding + bottomTabInset)
            }
            keyGearRoot.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
                scroll = scrollY
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    loggedInViewModel.onScroll(scroll)
                }
            }

            errorContainer.retry.setHapticClickListener {
                viewModel.load()
            }

            items.adapter =
                KeyGearItemsAdapter(
                    tracker,
                    { v ->
                        startActivity(
                            CreateKeyGearItemActivity.newInstance(requireContext()),
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                requireActivity(),
                                transitionPair(v)
                            ).toBundle()
                        )
                    },
                    { root, item ->
                        startActivity(
                            KeyGearItemDetailActivity.newInstance(
                                requireContext(),
                                item.fragments.keyGearItemFragment
                            ),
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                requireActivity(),
                                Pair(root, ITEM_BACKGROUND_TRANSITION_NAME)
                            ).toBundle()
                        )
                    }
                )
            items.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN))
            items.itemAnimator = SlideInItemAnimator()

            viewModel.data.observe(viewLifecycleOwner) { data ->
                if (data.isFailure) {
                    errorContainer.root.show()
                    contentContainer.remove()
                    return@observe
                }
                errorContainer.root.remove()
                contentContainer.show()
                data.getOrNull()?.let { bind(it) }
                if (!hasSentAutoAddedItems) {
                    hasSentAutoAddedItems = true
                    viewModel.sendAutoAddedItems()
                }
            }
        }
    }

    fun bind(data: KeyGearItemsQuery.Data) = with(binding) {
        binding.loadingSpinner.root.remove()
        (items.adapter as? KeyGearItemsAdapter)?.submitList(data.keyGearItems)
        items.show()

        if (
            data.keyGearItems.isEmpty() ||
            !data.keyGearItems.any { it.fragments.keyGearItemFragment.physicalReferenceHash == null }
        ) {
            illustration.show()
            title.show()
            description.show()
            items.updateMargin(top = BASE_MARGIN_QUINTUPLE)
        } else {
            illustration.remove()
            title.remove()
            description.remove()
            items.updateMargin(top = BASE_MARGIN_TRIPLE)
        }
    }

    companion object {
        const val ITEM_BACKGROUND_TRANSITION_NAME = "itemBackground"
    }
}
