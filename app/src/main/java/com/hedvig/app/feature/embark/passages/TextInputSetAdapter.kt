package com.hedvig.app.feature.embark.passages

import android.text.InputType
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkInputItemBinding
import com.hedvig.app.feature.embark.BIRTH_DATE
import com.hedvig.app.feature.embark.BIRTH_DATE_REVERSE
import com.hedvig.app.feature.embark.EMAIL
import com.hedvig.app.feature.embark.NORWEGIAN_POSTAL_CODE
import com.hedvig.app.feature.embark.PERSONAL_NUMBER
import com.hedvig.app.feature.embark.SWEDISH_POSTAL_CODE
import com.hedvig.app.feature.embark.validationCheck
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.viewBinding

class TextInputSetAdapter(val model: TextActionSetViewModel) :
    ListAdapter<TextFieldData, TextInputSetAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position, model)
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.embark_input_item)) {
        private val binding by viewBinding(EmbarkInputItemBinding::bind)
        fun bind(item: TextFieldData, position: Int, model: TextActionSetViewModel) {
            binding.apply {
                model.updateHasTextAndIsValidHashMap(position, false)
                textField.hint = item.placeholder
                when (item.mask) {
                    PERSONAL_NUMBER,
                    SWEDISH_POSTAL_CODE,
                    BIRTH_DATE,
                    BIRTH_DATE_REVERSE,
                    NORWEGIAN_POSTAL_CODE -> input.inputType = InputType.TYPE_CLASS_NUMBER
                    EMAIL -> input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                }
                input.onChange { text ->
                    if (item.mask == null) {
                        if (text.isBlank()) {
                            model.updateHasTextAndIsValidHashMap(position, false)
                        } else {
                            model.updateHasTextAndIsValidHashMap(position, true)
                        }
                    } else {
                        if (text.isNotBlank() && validationCheck(
                                item.mask, text
                            )
                        ) {
                            model.updateHasTextAndIsValidHashMap(position, true)
                        } else {
                            model.updateHasTextAndIsValidHashMap(position, false)
                        }
                    }
                }
            }
        }
    }
}

