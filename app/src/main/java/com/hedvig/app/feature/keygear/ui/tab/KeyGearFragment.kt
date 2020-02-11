package com.hedvig.app.feature.keygear.ui.tab

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemActivity
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailActivity
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.transitionPair
import kotlinx.android.synthetic.main.fragment_key_gear.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class KeyGearFragment : BaseTabFragment() {
    override val layout = R.layout.fragment_key_gear

    private val viewModel: KeyGearViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        items.adapter =
            KeyGearItemsAdapter({ v ->
                startActivity(
                    CreateKeyGearItemActivity.newInstance(requireContext()),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        transitionPair(v)
                    ).toBundle()
                )
            }, { v, item ->
                startActivity(
                    KeyGearItemDetailActivity.newInstance(
                        requireContext(),
                        item.fragments.keyGearItemFragment
                    ),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        Pair(v, ITEM_TRANSITION_NAME)
                    ).toBundle()
                )
            })
        items.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN))

        viewModel.data.observe(this) { d ->
            d?.let { data ->
                bind(data)
            }
        }
    }

    fun bind(data: KeyGearItemsQuery.Data) {
        loadingSpinner.remove()
        (items.adapter as? KeyGearItemsAdapter)?.items = data.keyGearItemsSimple
        items.show()

        if (data.keyGearItemsSimple.isEmpty()) {
            illustration.show()
            title.show()
            description.show()
        } else {
            illustration.remove()
            title.remove()
            description.remove()
        }
    }

    companion object {
        const val ITEM_TRANSITION_NAME = "itemPhoto"
    }
}
