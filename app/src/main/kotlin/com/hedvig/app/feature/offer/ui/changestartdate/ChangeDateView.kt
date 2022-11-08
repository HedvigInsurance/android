package com.hedvig.app.feature.offer.ui.changestartdate

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.hedvig.app.R
import com.hedvig.app.databinding.ChangeDateBinding
import com.hedvig.app.util.extensions.isToday
import com.hedvig.app.util.extensions.view.setHapticClickListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ChangeDateView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

  private val binding: ChangeDateBinding = ChangeDateBinding.inflate(LayoutInflater.from(context), this, true)
  private val dateFormat = DateTimeFormatter.ofPattern("d/M/yyyy")

  fun bind(
    title: String?,
    currentInsurerDisplayName: String?,
    startDate: LocalDate?,
    switchable: Boolean,
    datePickerListener: () -> Unit,
    switchListener: (Boolean) -> Unit,
  ) {
    setDateText(startDate)
    binding.title.text = title
    binding.title.isVisible = title != null
    binding.autoSetDateSwitch.isVisible = switchable
    binding.autoSetDateTitle.isVisible = switchable

    binding.datePickText.setHapticClickListener {
      datePickerListener()
    }

    if (switchable) {
      binding.autoSetDateSwitch.isChecked = startDate == null
      binding.autoSetDateSwitch.text =
        context.getString(hedvig.resources.R.string.OFFER_PLAN_EXIRES_TEXT, currentInsurerDisplayName)
      binding.autoSetDateSwitch.setOnCheckedChangeListener { _, isChecked ->
        switchListener(isChecked)
        setCheckedState(isChecked, startDate)
      }
      setCheckedState(startDate == null, startDate)
    }
  }

  private fun setCheckedState(isChecked: Boolean, startDate: LocalDate?) {
    if (isChecked) {
      binding.datePickText.text = null
    } else {
      binding.datePickText.setText(formatStartDate(startDate))
    }

    binding.datePickText.isEnabled = !isChecked
    binding.datePickLayout.isEnabled = !isChecked
  }

  fun setDateText(startDate: LocalDate?) {
    val dateText = formatStartDate(startDate)
    binding.datePickText.setText(dateText)
  }

  private fun formatStartDate(startDate: LocalDate?): String {
    return when {
      startDate == null -> ""
      startDate.isToday() -> context.getString(hedvig.resources.R.string.START_DATE_TODAY)
      else -> startDate.format(dateFormat)
    }
  }
}
