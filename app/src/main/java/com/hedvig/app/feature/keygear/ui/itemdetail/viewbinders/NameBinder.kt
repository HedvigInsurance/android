package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.key_gear_item_detail_name_section.view.*

class NameBinder(
    private val root: LinearLayout,
    private val model: KeyGearItemDetailViewModel
) {
    init {
        var isEditState = false
        root.addName.setHapticClickListener {
            if (isEditState) {
                hideEditName()
                root.addName.text =
                    root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_ITEM_NAME_EDIT_BUTTON)
                updateName()
                isEditState = false
            } else {
                focusEditName()
                root.addName.text =
                    root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_ITEM_NAME_SAVE_BUTTON)
                isEditState = true
            }

        }

        root.nameEditText.setDoneListener {
            hideEditName()
            updateName()
        }
    }

    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        val name = data.fragments.keyGearItemFragment.name
        root.name.text = name
    }

    private fun updateName() {
        val name = root.nameEditText.getText()
        model.updateItemName(name)
    }

    private fun focusEditName() {
        root.name.animate().alpha(0.0f).withEndAction {
            root.name.remove()
        }.duration = ANIMATE_DURATION

        root.nameEditText.apply {
            alpha = 0.0f
            show()
            root.nameEditText.animate().alpha(1.0f).withEndAction {
                nameEditText.openKeyBoard()
            }.duration = ANIMATE_DURATION
        }
    }

    private fun hideEditName() {
        root.nameEditText.apply {
            animate()
                .alpha(0f).withEndAction {
                    root.nameEditText.dismissKeyboard()
                    root.nameEditText.remove()
                }.duration = ANIMATE_DURATION
        }

        root.name.apply {
            alpha = 0.0f
            show()
            animate()
                .alpha(1.0f)
                .duration = ANIMATE_DURATION
        }
    }

    companion object {
        private const val ANIMATE_DURATION = 60L
    }
}
