package com.hedvig.android.feature.chat.legacy

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

internal fun EditText.onChange(callback: (String) -> Unit): TextWatcher {
  val watcher = object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
      callback(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
  }
  addTextChangedListener(watcher)
  return watcher
}
