package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.view.openKeyboard
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.key_gear_item_detail_name_section.view.*

class NameBinder(
    private val root: LinearLayout,
    private val model: KeyGearItemDetailViewModel,
    private val tracker: KeyGearTracker
) {
    init {
        var isEditState = false
        root.addName.setHapticClickListener {
            if (isEditState) {
                tracker.saveName()
                hideEditName()
                root.addName.text =
                    root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_ITEM_NAME_EDIT_BUTTON)
                updateName()
                isEditState = false
            } else {
                tracker.editName()
                focusEditName()
                root.addName.text =
                    root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_ITEM_NAME_SAVE_BUTTON)
                isEditState = true
            }

        }

        root.nameEditText.setOnEditorActionListener { _, _, _ ->
            tracker.saveName()
            hideEditName()
            root.addName.text =
                root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_ITEM_NAME_EDIT_BUTTON)
            updateName()
            isEditState = false
            true
        }
    }

    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        val name = data.fragments.keyGearItemFragment.name
        val category =
            root.context.resources.getString(data.fragments.keyGearItemFragment.category.label)

        if (name.isNullOrBlank()) {
            root.nameEditText.setText("")
            root.name.text = category
        } else {
            root.nameEditText.setText(name)
            root.nameEditText.setSelection(name.length)
            root.name.text = name
        }
    }

    private fun updateName() {
        val name = root.nameEditText.text.toString()
        model.updateItemName(name)
    }

    private fun focusEditName() {
        root.name
            .animate()
            .alpha(0.0f)
            .withEndAction {
                root.name.remove()
            }
            .setDuration(ANIMATE_DURATION)
            .start()

        root.nameEditTextContainer.apply {
            alpha = 0.0f
            show()
            animate()
                .alpha(1.0f)
                .withEndAction {
                    root.nameEditText.requestFocus()
                    root.nameEditText.openKeyboard()
                }
                .setDuration(ANIMATE_DURATION)
                .start()
        }
    }

    private fun hideEditName() {
        root.nameEditTextContainer.apply {
            animate()
                .alpha(0f)
                .withEndAction {
                    root.nameEditText.dismissKeyboard()
                    root.nameEditTextContainer.remove()
                }
                .setDuration(ANIMATE_DURATION)
                .start()
        }

        root.name.apply {
            alpha = 0.0f
            show()
            animate()
                .alpha(1.0f)
                .setDuration(ANIMATE_DURATION)
                .start()
        }
    }

    companion object {
        private const val ANIMATE_DURATION = 60L
    }
}
