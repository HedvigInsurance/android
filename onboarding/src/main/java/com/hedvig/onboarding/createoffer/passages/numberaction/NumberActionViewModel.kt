package com.hedvig.onboarding.createoffer.passages.numberaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NumberActionViewModel(
    private val data: NumberActionParams,
) : ViewModel() {
    private val _valid = MutableLiveData(false)
    val valid: LiveData<Boolean> = _valid

    fun validate(input: CharSequence?) {
        _valid.value = when {
            input?.toString()?.toIntOrNull() != null -> {
                isValid(input.toString().toInt())
            }
            else -> false
        }
    }

    private fun isValid(number: Int) = when {
        data.minValue != null && data.maxValue != null -> {
            number in data.minValue..data.maxValue
        }
        data.maxValue != null -> {
            number <= data.maxValue
        }
        data.minValue != null -> {
            number >= data.minValue
        }
        else -> true
    }
}
