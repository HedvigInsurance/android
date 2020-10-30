package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.KeyGearItemDetailNameSectionBinding
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.view.openKeyboard
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show

class NameBinder(
    private val binding: KeyGearItemDetailNameSectionBinding,
    private val model: KeyGearItemDetailViewModel,
    private val tracker: KeyGearTracker
) {
    init {
        var isEditState = false
        binding.addName.setHapticClickListener {
            if (isEditState) {
                tracker.saveName()
                hideEditName()
                binding.addName.setText(R.string.KEY_GEAR_ITEM_VIEW_ITEM_NAME_EDIT_BUTTON)
                updateName()
                isEditState = false
            } else {
                tracker.editName()
                focusEditName()
                binding.addName.setText(R.string.KEY_GEAR_ITEM_VIEW_ITEM_NAME_SAVE_BUTTON)
                isEditState = true
            }

        }

        binding.nameEditText.setOnEditorActionListener { _, _, _ ->
            tracker.saveName()
            hideEditName()
            binding.addName.setText(R.string.KEY_GEAR_ITEM_VIEW_ITEM_NAME_EDIT_BUTTON)
            updateName()
            isEditState = false
            true
        }
    }

    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        val name = data.fragments.keyGearItemFragment.name
        val category =
            binding.root.context.getString(data.fragments.keyGearItemFragment.category.label)

        if (name.isNullOrBlank()) {
            binding.nameEditText.setText("")
            binding.name.text = category
        } else {
            binding.nameEditText.setText(name)
            binding.nameEditText.setSelection(name.length)
            binding.name.text = name
        }
    }

    private fun updateName() {
        val name = binding.nameEditText.text.toString()
        model.updateItemName(name)
    }

    private fun focusEditName() {
        binding.name
            .animate()
            .alpha(0.0f)
            .withEndAction {
                binding.name.remove()
            }
            .setDuration(ANIMATE_DURATION)
            .start()

        binding.nameEditTextContainer.apply {
            alpha = 0.0f
            show()
            animate()
                .alpha(1.0f)
                .withEndAction {
                    binding.nameEditText.requestFocus()
                    binding.nameEditText.openKeyboard()
                }
                .setDuration(ANIMATE_DURATION)
                .start()
        }
    }

    private fun hideEditName() {
        binding.nameEditTextContainer.apply {
            animate()
                .alpha(0f)
                .withEndAction {
                    binding.nameEditText.dismissKeyboard()
                    binding.nameEditTextContainer.remove()
                }
                .setDuration(ANIMATE_DURATION)
                .start()
        }

        binding.name.apply {
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
