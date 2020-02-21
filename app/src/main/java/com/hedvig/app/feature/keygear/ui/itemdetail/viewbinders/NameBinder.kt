package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.key_gear_item_detail_name_section.view.*

class NameBinder(
    private val root: LinearLayout,
    private val baseActivity: BaseActivity,
    private val model: KeyGearItemDetailViewModel
) {
    init {
        root.addName.setHapticClickListener {
            focusEditName()
        }

        root.nameEditText.setDoneListener {
            hideEditName()
            updateName()
        }

        root.saveName.setHapticClickListener {
            updateName()
            hideEditName()

        }
    }

    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        val name = data.fragments.keyGearItemFragment.name
        root.name.text = name
    }

    private fun updateName() {
        val name = root.nameEditText.getText()
        model.updateItemName(name)
        model.data.observe(baseActivity) { data ->
            data?.let {
                val newName = data.fragments.keyGearItemFragment.name
                root.name.text = newName
            }

        }
    }

    private fun focusEditName() {
        root.name.animate().alpha(0.0f).withEndAction {
            root.name.remove()
        }.duration = ANIMATE_DURATION

        root.addName.animate().alpha(0.0f).withEndAction {
            root.addName.remove()
        }.duration = ANIMATE_DURATION


        root.nameEditText.apply {
            alpha = 0.0f
            show()
            root.nameEditText.animate().alpha(1.0f).withEndAction {
                nameEditText.openKeyBoard()
            }.duration = ANIMATE_DURATION
        }

        root.saveName.apply {
            alpha = 0.0f
            show()
            root.saveName
                .animate()
                .alpha(1.0f)
                .duration = ANIMATE_DURATION
        }
    }

    private fun hideEditName() {
        root.saveName.animate()
            .alpha(0.0f)
            .withEndAction {
                root.saveName.remove()
            }.duration = ANIMATE_DURATION

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

        root.addName.apply {
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
