package com.hedvig.app.feature.embark.passages

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
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
                if (item.mask == EMAIL) {
                    input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                }

                if (item.mask == BIRTH_DATE ||
                    item.mask == BIRTH_DATE_REVERSE ||
                    item.mask == PERSONAL_NUMBER ||
                    item.mask == SWEDISH_POSTAL_CODE ||
                    item.mask == NORWEGIAN_POSTAL_CODE
                ) {
                    input.keyListener = DigitsKeyListener.getInstance(
                        when (item.mask) {
                            PERSONAL_NUMBER,
                            BIRTH_DATE,
                            BIRTH_DATE_REVERSE -> "0123456789-"
                            NORWEGIAN_POSTAL_CODE -> "0123456789"
                            SWEDISH_POSTAL_CODE -> "0123456789 "
                            else -> "0123456789- "
                        }
                    )
                    var prevLength = 0
                    input.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            charSequence: CharSequence,
                            i: Int,
                            i1: Int,
                            i2: Int
                        ) {
                            prevLength = input.text.toString().length
                        }

                        override fun onTextChanged(
                            charSequence: CharSequence,
                            i: Int,
                            i1: Int,
                            i2: Int
                        ) {
                        }

                        override fun afterTextChanged(editable: Editable) {
                            val length = editable.length
                            when (item.mask) {
                                PERSONAL_NUMBER -> {
                                    if (prevLength < length && length == 6) {
                                        editable.append("-")
                                    }
                                }
                                SWEDISH_POSTAL_CODE -> {
                                    if (prevLength < length && length == 3) {
                                        editable.append(" ")
                                    }
                                }
                                BIRTH_DATE -> {
                                    if (prevLength < length && (length == 4 || length == 7)) {
                                        editable.append("-")
                                    }
                                }
                                BIRTH_DATE_REVERSE -> {
                                    if (prevLength < length && (length == 2 || length == 5)) {
                                        editable.append("-")
                                    }
                                }
                            }
                        }
                    })
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

