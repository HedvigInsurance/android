package com.hedvig.app.feature.embark.passages.datepicker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.util.extensions.epochMillisToLocalDate
import java.time.LocalDateTime

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

    private fun epochMillisToLocalDate(epochMillis: Long) = epochMillis.epochMillisToLocalDate()
}
