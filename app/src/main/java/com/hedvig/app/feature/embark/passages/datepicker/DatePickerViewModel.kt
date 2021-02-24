package com.hedvig.app.feature.embark.passages.datepicker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class DatePickerViewModel : ViewModel() {

    val selectedDate = MutableLiveData<LocalDateTime?>(null)
    val showDatePicker = MutableLiveData<Long?>()
    private var selectedEpochMillis: Long? = null

    fun onDateSelected(epochMillis: Long) {
        selectedEpochMillis = epochMillis
        selectedDate.value = epochMillisToLocalDate(epochMillis)
    }

    fun onShowDatePicker() {
        showDatePicker.value = selectedEpochMillis
    }

    private fun epochMillisToLocalDate(epochMillis: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
    }

}
