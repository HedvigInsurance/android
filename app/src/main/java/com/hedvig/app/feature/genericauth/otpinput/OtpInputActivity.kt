package com.hedvig.app.feature.genericauth.otpinput

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.hedvig.app.BaseActivity
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.androidx.viewmodel.ext.android.viewModel

class OtpInputActivity : BaseActivity() {
    val model: OtpInputViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.compatSetDecorFitsSystemWindows(false)

        setContent {
            val value by model.input.collectAsState()
            HedvigTheme {
                OtpInputScreen(
                    onUpClick = ::finish,
                    onInputChanged = model::setInput,
                    onOpenExternalApp = {},
                    onResendCode = {},
                    onSubmitCode = {},
                    inputValue = value,
                    error = null, // TODO
                )
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, OtpInputActivity::class.java)
    }
}

class OtpInputViewModel : ViewModel() {
    private val _input = MutableStateFlow("")
    val input = _input.asStateFlow()

    fun setInput(value: String) {
        _input.value = value
    }
}
